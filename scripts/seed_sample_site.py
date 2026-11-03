#!/usr/bin/env python3
"""Builds the "중고천국" sample site through the CMS's own APIs.

Run it against an empty dev database with the server up:
    python3 scripts/seed_sample_site.py [--base-url http://localhost:8080] [--admin-password ...]

Everything goes through the public/admin APIs (no SQL), so the data obeys the same validation and
business rules as data entered from the screens.
"""
import argparse
import mimetypes
import uuid
from pathlib import Path
import http.cookiejar
import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timedelta

IMAGES = Path(__file__).with_name('sample-images')
MEDIA = Path(__file__).with_name('sample-media')


class Client:
    def __init__(self, base_url, zone):
        self.base_url = base_url
        self.zone = zone
        self.cookies = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(self.cookies))
        self.request('GET', '/api/auth/csrf')

    def _csrf(self):
        return next((c.value for c in self.cookies if c.name == 'XSRF-TOKEN'), None)

    def request(self, method, path, body=None):
        headers = {'Accept': 'application/json'}
        if body is not None:
            headers['Content-Type'] = 'application/json'
        if self.zone == 'admin':
            headers['X-CMS-Zone'] = 'admin'
        token = self._csrf()
        if token:
            headers['X-XSRF-TOKEN'] = token
        data = json.dumps(body, ensure_ascii=False).encode() if body is not None else None
        req = urllib.request.Request(self.base_url + path, data=data, method=method, headers=headers)
        try:
            with self.opener.open(req) as res:
                raw = res.read()
                location = res.headers.get('Location')
                return (json.loads(raw) if raw else None), location
        except urllib.error.HTTPError as e:
            sys.exit(f'{method} {path} -> {e.code}: {e.read().decode(errors="replace")}')

    def get(self, path):
        return self.request('GET', path)[0]

    def put(self, path, body):
        return self.request('PUT', path, body)[0]

    def post(self, path, body=None):
        return self.request('POST', path, body if body is not None else {})[0]

    def create(self, path, body):
        """POSTs and returns the new id from the Location header (or the body's id)."""
        payload, location = self.request('POST', path, body)
        if location:
            return int(location.rstrip('/').split('/')[-1])
        return payload['id']

    def upload(self, file_name, alternative_text, folder=IMAGES):
        """Uploads a sample file (images: see sample-images/credits.json) and returns its file id."""
        boundary = uuid.uuid4().hex
        content_type = mimetypes.guess_type(file_name)[0] or 'application/octet-stream'
        body = (f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{file_name}"\r\n'
                f'Content-Type: {content_type}\r\n\r\n').encode() + (folder / file_name).read_bytes() + f'\r\n--{boundary}--\r\n'.encode()
        headers = {'Content-Type': f'multipart/form-data; boundary={boundary}', 'Accept': 'application/json',
                   'X-XSRF-TOKEN': self._csrf()}
        if self.zone == 'admin':
            headers['X-CMS-Zone'] = 'admin'
        query = urllib.parse.urlencode({'alternativeText': alternative_text})
        req = urllib.request.Request(f'{self.base_url}/api/files?{query}', data=body, method='POST', headers=headers)
        try:
            with self.opener.open(req) as res:
                return json.loads(res.read())['id']
        except urllib.error.HTTPError as e:
            sys.exit(f'upload {file_name} -> {e.code}: {e.read().decode(errors="replace")}')

    def login(self, username, password, user_type):
        self.post('/api/auth/login', {'username': username, 'password': password, 'userType': user_type})
        return self


def at(days, hour=10):
    moment = (datetime.now() + timedelta(days=days)).replace(hour=hour, minute=0, second=0, microsecond=0)
    return moment.strftime('%Y-%m-%dT%H:%M:%S')


def p(*paragraphs):
    return ''.join(f'<p>{text}</p>' for text in paragraphs)


def seed(base_url, admin_password):
    admin = Client(base_url, 'admin').login('admin', admin_password, 'ADMIN')

    if any(site['name'] == '중고천국' for site in admin.get('/api/admin/sites?size=100')['content']):
        sys.exit('중고천국 사이트가 이미 있습니다. 빈 데이터베이스에서 실행해 주세요.')

    site = {
        'name': '중고천국', 'domain': 'www.junggo-heaven.example', 'theme': 'market', 'locale': 'ko', 'isDefault': True,
        'description': '검수부터 안전결제까지, 믿고 사고파는 중고거래 플랫폼', 'disabledFeatures': [],
    }
    site_id = admin.create('/api/admin/sites', site)
    admin.create('/api/admin/configs', {'key': 'site.footer', 'type': 'TEXT',
                                        'value': 'ⓒ 2026 중고천국 · 사업자등록번호 000-00-00000 · 고객센터 1588-0000 (평일 09:00~18:00)',
                                        'description': '사이트 하단 문구'})
    admin.create('/api/admin/configs', {'key': 'site.contact', 'type': 'TEXT', 'value': '1588-0000',
                                        'description': '고객센터 대표 번호'})

    admin.put('/api/admin/policies/PRIVACY_CONSENT', {'contentHtml': (
        '<h3>개인정보 수집·이용 동의</h3>'
        '<ul><li>수집 항목: 아이디, 비밀번호, 이름, 이메일, 휴대전화 번호, 배송지 주소</li>'
        '<li>이용 목적: 회원 식별, 중고 상품 주문·배송, 안전거래 분쟁 처리, 고객 문의 응대</li>'
        '<li>보유 기간: 회원 탈퇴 시까지 (관계 법령에 따라 거래 기록은 5년 보관)</li></ul>'
        '<p>동의를 거부할 수 있으나, 거부하면 회원가입과 주문을 할 수 없습니다.</p>')})
    admin.put('/api/admin/policies/PRIVACY_POLICY', {'contentHtml': (
        '<h3>중고천국 개인정보처리방침</h3>'
        + p('중고천국(이하 "회사")은 이용자의 개인정보를 소중히 여기며 「개인정보 보호법」을 준수합니다.')
        + '<h4>1. 처리하는 개인정보 항목</h4>' + p('아이디, 이름, 이메일, 휴대전화 번호, 배송지 주소, 결제 기록')
        + '<h4>2. 개인정보의 제3자 제공</h4>' + p('배송을 위해 택배사에 수령인 이름·연락처·주소를 제공합니다.')
        + '<h4>3. 개인정보 보호책임자</h4>' + p('개인정보 보호책임자: 운영팀장 / privacy@junggo-heaven.example'))})

    for order, (name, english, children) in enumerate([
        ('상품 등급', 'GRADE', [('S급 (미개봉)', 'S'), ('A급 (사용감 거의 없음)', 'A'), ('B급 (생활 흠집)', 'B'), ('C급 (기능 정상, 외관 하자)', 'C')]),
        ('거래 방식', 'TRADE', [('택배 거래', 'DELIVERY'), ('직거래', 'DIRECT'), ('매장 픽업', 'PICKUP')]),
    ], start=1):
        parent = admin.create('/api/admin/codes', {'order': order, 'name': name, 'englishName': english, 'isUsing': True})
        for child_order, (child, child_english) in enumerate(children, start=1):
            admin.create('/api/admin/codes', {'parentId': parent, 'order': child_order, 'name': child,
                                              'englishName': child_english, 'isUsing': True})

    home = admin.create('/api/admin/contents', {'title': '홈 (랜딩 페이지)', 'status': 'PUBLISHED', 'contentHtml': (
        '<section class="hero"><h1>버리기 아까운 물건에, 두 번째 주인을</h1>'
        '<p class="hero-description">전문 검수와 안전결제로 믿고 사고파는 중고천국</p>'
        '<div data-block="product-search"></div><div data-block="greeting"></div></section>'
        '<div data-block="shortcuts"></div>'
        '<div data-block="new-products"></div>'
        '<section class="home-section"><div class="section-head"><h2>중고천국이 다른 이유</h2></div><div class="feature-grid">'
        '<div class="feature-card"><h3>🔍 전문 검수</h3><p>검수센터에서 기능·외관을 점검하고 S/A/B/C 등급을 매깁니다.</p></div>'
        '<div class="feature-card"><h3>🔒 안전결제</h3><p>구매 확정 전까지 대금을 중고천국이 안전하게 보관합니다.</p></div>'
        '<div class="feature-card"><h3>↩️ 7일 환불</h3><p>검수 내용과 다르면 수령 후 7일 안에 환불해 드립니다.</p></div>'
        '</div></section>'
        '<div class="home-columns"><div data-block="open-programs"></div><div data-block="latest-posts"></div></div>'),
        'memo': '사이트 설정의 홈 화면 컨텐츠로 지정됨'})
    admin.put(f'/api/admin/sites/{site_id}', {**site, 'homeContentId': home})

    about = admin.create('/api/admin/contents', {'title': '중고천국 소개', 'status': 'PUBLISHED', 'contentHtml': (
        '<h2>버리기 아까운 물건에 두 번째 주인을</h2>'
        + p('중고천국은 2026년에 문을 연 검수형 중고거래 플랫폼입니다. 전문 검수사가 상품 상태를 직접 확인하고 등급을 매겨, '
            '사진만 보고 사는 중고거래의 불안을 줄였습니다.')
        + '<ul><li><strong>전문 검수</strong> — 모든 상품을 검수센터에서 기능·외관 점검 후 S/A/B/C 등급으로 표시합니다.</li>'
          '<li><strong>안전결제</strong> — 구매 확정 전까지 대금을 중고천국이 보관합니다.</li>'
          '<li><strong>7일 환불</strong> — 검수 내용과 다르면 수령 후 7일 안에 환불해 드립니다.</li></ul>')})
    guide = admin.create('/api/admin/contents', {'title': '안전거래 가이드', 'status': 'PUBLISHED', 'contentHtml': (
        '<h2>안전한 중고거래를 위한 5가지 약속</h2>'
        '<ol><li>판매자가 외부 메신저나 계좌 이체를 요구하면 거래를 중단하세요.</li>'
        '<li>직거래는 사람이 많은 공공장소에서, 물건을 확인한 뒤 결제하세요.</li>'
        '<li>시세보다 지나치게 싼 상품은 한 번 더 의심하세요.</li>'
        '<li>택배 거래는 반드시 중고천국 안전결제를 이용하세요.</li>'
        '<li>사기가 의심되면 즉시 <a href="/reports/new">사기 의심 신고</a>를 접수해 주세요.</li></ol>')})
    fee = admin.create('/api/admin/contents', {'title': '판매 수수료 안내', 'status': 'PUBLISHED', 'contentHtml': (
        '<h2>판매 수수료</h2><table><thead><tr><th>판매 금액</th><th>수수료</th></tr></thead><tbody>'
        '<tr><td>10만 원 미만</td><td>5%</td></tr><tr><td>10만 원 ~ 100만 원</td><td>3.5%</td></tr>'
        '<tr><td>100만 원 이상</td><td>2.5%</td></tr></tbody></table>'
        + p('검수비는 무료이며, 검수에서 반려된 상품은 왕복 택배비만 부담합니다.'))})
    credits = json.loads((IMAGES / 'credits.json').read_text())
    photo_credits = admin.create('/api/admin/contents', {'title': '사진 출처', 'status': 'PUBLISHED', 'contentHtml': (
        '<h2>사진 출처</h2>' + p('샘플 상품·프로그램 사진은 위키미디어 공용의 자유 라이선스 이미지를 축소해 사용했습니다.')
        + '<ul>' + ''.join(
            f'<li><a href="{c["source"]}">{c["title"].removeprefix("File:")}</a> — {c["author"] or "작자 미상"}, '
            + (f'<a href="{c["licenseUrl"]}">{c["license"]}</a>' if c.get('licenseUrl') else c['license']) + '</li>'
            for c in credits.values()) + '</ul>')})
    admin.create('/api/admin/contents', {'title': '채용 안내 (준비 중)', 'status': 'DRAFT',
                                         'contentHtml': p('검수사 채용 공고를 준비하고 있습니다.'), 'memo': '공개 전 인사팀 검토 필요'})

    about_menu = admin.create('/api/admin/menus', {'name': '소개', 'type': 'CONTENT', 'targetId': about, 'sequence': 1})
    for sequence, (name, feature) in enumerate([
        ('커뮤니티', 'BOARD'), ('공지사항', 'ARCHIVE'), ('상품', 'PRODUCT'), ('프로그램', 'PROGRAM'),
        ('질문답변', 'QNA'), ('신고', 'REPORT'), ('지도', 'MAP'), ('스트리밍', 'MEDIA'),
    ], start=2):
        admin.create('/api/admin/menus', {'name': name, 'type': 'FEATURE', 'feature': feature, 'sequence': sequence})
    admin.create('/api/admin/menus', {'parentId': about_menu, 'name': '안전거래 가이드', 'type': 'CONTENT',
                                      'targetId': guide, 'sequence': 1})
    admin.create('/api/admin/menus', {'parentId': about_menu, 'name': '판매 수수료 안내', 'type': 'CONTENT',
                                      'targetId': fee, 'sequence': 2})
    admin.create('/api/admin/menus', {'parentId': about_menu, 'name': '사진 출처', 'type': 'CONTENT',
                                      'targetId': photo_credits, 'sequence': 3})

    board_defaults = {'type': 'NORMAL', 'isMemberAccessible': True, 'isMemberReadable': True, 'isGuestAccessible': True,
                      'isGuestReadable': True, 'supportsNotice': True, 'supportsComment': True, 'supportsLike': True,
                      'supportsAttachment': True, 'maximumFileCount': 5, 'pageSize': 10}
    review_board = admin.create('/api/admin/boards', {**board_defaults, 'name': '거래 후기', 'description': '중고천국에서 사고판 이야기',
                                                      'isMemberWritable': True, 'supportsSecret': False})
    free_board = admin.create('/api/admin/boards', {**board_defaults, 'name': '자유게시판', 'description': '중고 이야기 무엇이든',
                                                    'isMemberWritable': True, 'isGuestWritable': True, 'supportsSecret': True})

    notices = admin.create('/api/admin/archives', {'name': '공지사항', 'description': '중고천국 소식과 운영 안내',
                                                   'type': 'NORMAL', 'supportsComment': False, 'pageSize': 10})
    for title, body, status in [
        ('사기 거래 주의 안내', p('최근 외부 메신저로 유도해 선입금을 요구하는 사례가 늘고 있습니다. 안전결제 외의 송금 요구에는 응하지 마세요.'), 'PUBLISHED'),
        ('검수센터 운영 시간 변경', p('12월부터 검수센터가 토요일에도 운영합니다 (10:00~15:00).'), 'PUBLISHED'),
        ('11월 정기 점검 안내 (11/12 02:00~05:00)', p('서비스 안정화를 위한 정기 점검이 진행됩니다. 점검 중에는 주문과 결제가 일시 중단됩니다.'), 'PUBLISHED'),
        ('중고천국 정식 오픈 안내', p('중고천국이 정식으로 문을 열었습니다. 오픈 기념으로 첫 구매 10% 할인 쿠폰을 드립니다.'), 'PUBLISHED'),
        ('연말 감사 이벤트 예고', p('12월 중순 공개 예정입니다.'), 'DRAFT'),
    ]:
        admin.create(f'/api/admin/archives/{notices}/articles', {'title': title, 'contentHtml': body, 'status': status})

    admin.put('/api/admin/members/join-settings',
              {'nameFormat': 'FULL', 'collectedItems': ['EMAIL', 'MOBILE_PHONE', 'ADDRESS']})
    members = [
        ('happybuyer', '김하늘', 'sky@example.com', '010-1234-5678', '서울특별시 마포구 월드컵북로 400', '101동 1203호'),
        ('retro_mania', '이도윤', 'doyun@example.com', '010-2345-6789', '부산광역시 부산진구 중앙대로 672', '3층'),
        ('camping_lee', '박서연', 'seoyeon@example.com', '010-3456-7890', '대전광역시 서구 둔산로 100', '502호'),
        ('gadget_kim', '최민준', 'minjun@example.com', '010-4567-8901', '경기도 성남시 분당구 판교역로 235', 'A동 7층'),
    ]
    clients = {}
    for username, name, email, phone, address, detail in members:
        guest = Client(base_url, 'site')
        guest.create('/api/members', {
            'username': username, 'password': 'junggo1234!', 'passwordCheck': 'junggo1234!', 'name': name, 'email': email,
            'mobilePhoneNumber': phone, 'postalCode': '00000', 'address': address, 'detailAddress': detail,
            'isMailReceptionAgreed': True, 'isSmsReceptionAgreed': username != 'retro_mania',
            'isMarketingUsageAgreed': username in ('happybuyer', 'gadget_kim'), 'isPrivacyConsentAgreed': True,
        })
        clients[username] = guest.login(username, 'junggo1234!', 'MEMBER')

    sky, doyun, seoyeon, minjun = (clients[name] for name in ('happybuyer', 'retro_mania', 'camping_lee', 'gadget_kim'))

    first_review = sky.create(f'/api/boards/{review_board}/articles', {'title': '아이폰 검수 상태 그대로 왔어요',
        'contentHtml': p('A급이라고 해서 반신반의했는데 정말 흠집 하나 없네요. 배터리 성능도 상세 페이지와 같았습니다.')})
    seoyeon.create(f'/api/boards/{review_board}/articles', {'title': '캠핑 의자 직거래 후기',
        'contentHtml': p('대전 검수센터에서 픽업했어요. 직원분이 펼치는 법까지 알려주셔서 좋았습니다.')})
    doyun.create(f'/api/boards/{review_board}/articles', {'title': '게임기 팔아봤습니다',
        'contentHtml': p('검수 신청하고 사흘 만에 판매 완료됐어요. 수수료도 생각보다 저렴합니다.')})
    sky.post(f'/api/boards/{review_board}/articles/{first_review}/likes')
    minjun.post(f'/api/boards/{review_board}/articles/{first_review}/likes')
    minjun.create(f'/api/boards/{review_board}/articles/{first_review}/comments', {'content': '저도 같은 판매자한테 샀는데 만족했어요!'})
    sky.create(f'/api/boards/{review_board}/articles/{first_review}/comments', {'content': '역시 검수 받은 게 마음 편하네요 :)'})

    minjun.create(f'/api/boards/{free_board}/articles', {'title': '중고 노트북 고를 때 꼭 보는 것',
        'contentHtml': p('배터리 사이클 수, 힌지 유격, 키보드 눌림은 꼭 확인하세요. 검수 리포트에 다 나와서 편하더라고요.')})
    Client(base_url, 'site').create(f'/api/boards/{free_board}/articles', {'title': '비회원도 글 쓸 수 있네요',
        'contentHtml': p('구경만 하다가 처음 글 남겨봐요.'), 'authorName': '지나가던사람', 'password': 'guest1234'})
    doyun.create(f'/api/boards/{free_board}/articles', {'title': '판매자에게만 보이는 질문 (비밀글)',
        'contentHtml': p('직거래 장소를 바꿀 수 있을까요?'), 'isSecret': True})

    archive = admin.create('/api/admin/archives', {'name': '거래 자료실', 'description': '거래에 필요한 양식과 가이드',
                                                   'type': 'NORMAL', 'supportsComment': True, 'pageSize': 10})
    for title, body in [
        ('중고거래 표준 계약서 양식', p('직거래 시 작성하는 표준 계약서 양식입니다. 인쇄해서 거래 당일에 지참하세요.')),
        ('상품 사진 잘 찍는 법', p('자연광, 흰 배경, 하자 부위 근접 촬영 — 세 가지만 지켜도 판매 속도가 두 배 빨라집니다.')),
        ('검수 등급 기준표 (2026년 개정)', p('S/A/B/C 등급별 외관·기능 기준을 정리했습니다.')),
    ]:
        admin.create(f'/api/admin/archives/{archive}/articles', {'title': title, 'contentHtml': body, 'status': 'PUBLISHED'})

    admin.put('/api/admin/map/settings', {
        'provider': 'KAKAO', 'directionsEnabled': True, 'branchesEnabled': True,
        'officeName': '중고천국 본사', 'officeAddress': '서울특별시 강남구 테헤란로 152', 'officeDetailAddress': '중고천국빌딩 5층',
        'officeLatitude': 37.5000, 'officeLongitude': 127.0364, 'officePhoneNumber': '1588-0000',
        'officeBusinessHours': '평일 09:00~18:00 (점심 12:00~13:00)',
        'officeDescription': '2호선 강남역 1번 출구에서 도보 5분입니다. 건물 지하 주차장을 1시간 무료로 이용할 수 있습니다.',
    })
    for sequence, (name, address, detail, lat, lng, phone, hours, desc) in enumerate([
        ('강남 검수센터', '서울특별시 강남구 강남대로 396', '지하 1층', 37.4979, 127.0276, '02-555-0101', '매일 10:00~20:00', '디지털 기기 전문 검수, 당일 매입 상담'),
        ('성수 픽업라운지', '서울특별시 성동구 아차산로 17', '1층', 37.5446, 127.0557, '02-555-0102', '화~일 11:00~21:00', '가구·생활용품 직접 보고 구매'),
        ('부산 서면점', '부산광역시 부산진구 중앙대로 692', '2층', 35.1578, 129.0600, '051-555-0103', '매일 10:00~19:00', '부산·경남 지역 매입과 픽업'),
        ('대전 둔산점', '대전광역시 서구 대덕대로 211', '1층', 36.3510, 127.3850, '042-555-0104', '월~토 10:00~19:00', '캠핑·레저 용품 특화 매장'),
    ], start=1):
        admin.create('/api/admin/map/branches', {'name': name, 'address': address, 'detailAddress': detail, 'latitude': lat,
                                                 'longitude': lng, 'phoneNumber': phone, 'businessHours': hours,
                                                 'description': desc, 'sequence': sequence, 'isUsing': True})

    grade_options = [{'name': 'A급', 'description': '사용감 거의 없음', 'additionalPrice': 0},
                     {'name': 'S급', 'description': '미개봉 또는 리퍼 신품', 'additionalPrice': 0}]
    product_specs = [
        ('아이폰 15 Pro 256GB 내추럴 티타늄', '배터리 성능 94%, 액정 무결점', 980000, 3, 'iphone-15-pro.jpg',
         [{'name': 'A급', 'description': '배터리 90% 이상', 'additionalPrice': 0},
          {'name': 'S급', 'description': '미개봉 리퍼', 'additionalPrice': 150000}]),
        ('맥북 에어 M2 13인치 스타라이트 16GB/512GB', '배터리 사이클 87회, 정품 충전기 포함', 1050000, 2, 'macbook-air-m2.jpg',
         [{'name': 'B급', 'description': '상판 생활 흠집', 'additionalPrice': 0},
          {'name': 'A급', 'description': '외관 미세 사용감', 'additionalPrice': 100000}]),
        ('닌텐도 스위치 네온 블루/레드', '조이콘 쏠림 없음, 독 포함 박스 풀구성', 230000, 5, 'nintendo-switch.jpg', []),
        ('다이슨 V10 앱솔루트 무선청소기', '배터리 교체 완료, 헤드 3종', 280000, 2, 'dyson-v10.jpg', grade_options[:1]),
        ('헬리녹스 그라운드 체어 2개 세트', '캠핑 4회 사용, 수납 파우치 포함', 120000, 4, 'helinox-ground-chair.jpg', []),
        ('LG 그램 16 (2023) i7/16GB', '키보드 각인 깨끗, 화면 번인 없음', 890000, 1, 'lg-gram-16.jpg', []),
        ('소니 WH-1000XM3 블랙', '이어패드 새 제품으로 교체', 150000, 6, 'sony-wh-1000xm3.jpg', []),
        ('이케아 포엥 암체어 (쿠션 포함)', '성수 픽업라운지 직접 수령', 70000, 2, 'ikea-poang.jpg', []),
    ]
    products = []
    for name, description, price, quantity, image, options in product_specs:
        products.append(admin.create('/api/admin/products', {
            'name': name, 'description': description, 'price': price, 'availableQuantity': quantity, 'isEnabled': True,
            'options': options, 'imageFileId': admin.upload(image, name),
            'contentHtml': '<h3>검수 리포트</h3><ul><li>' + description.replace(', ', '</li><li>') + '</li></ul>'
                           + p('중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.'),
        }))
    admin.create('/api/admin/products', {'name': '갤럭시 Z 플립4 (판매 중지)', 'description': '판매자 요청으로 판매 중지',
                                         'price': 350000, 'availableQuantity': 0, 'isEnabled': False, 'options': [],
                                         'imageFileId': admin.upload('galaxy-z-flip4.jpg', '갤럭시 Z 플립4'),
                                         'contentHtml': p('판매가 중지된 상품입니다.')})
    iphone, macbook, switch, dyson, chair = products[:5]

    welcome = admin.create('/api/admin/discounts', {'name': '오픈 기념 10% 할인', 'description': '모든 상품 첫 구매 할인',
                                                    'rate': 10, 'isUsing': True})
    admin.create('/api/admin/discounts', {'productId': switch, 'name': '스위치 주말 특가', 'description': '닌텐도 스위치 한정 15%',
                                          'rate': 15, 'isUsing': True})
    admin.create('/api/admin/discounts', {'name': '추석 감사 할인 (종료)', 'description': '지난 이벤트', 'rate': 20, 'isUsing': False})

    def order(client, product, receiver, phone, address, option=None, discount=None, quantity=1, pay=True):
        created = client.post('/api/orders', {'productId': product, 'productOptionId': option, 'quantity': quantity,
                                              'discountId': discount, 'receiverName': receiver, 'phoneNumber': phone,
                                              'postalCode': '00000', 'address': address, 'detailAddress': ''})
        if pay:
            client.post('/api/payments', {'targetType': 'ORDER', 'targetId': created['id'],
                                          'amount': created['totalPrice'], 'method': 'CARD'})
        return created['id']

    iphone_options = admin.get(f'/api/products/{iphone}')['options']
    first = order(sky, iphone, '김하늘', '010-1234-5678', members[0][4], option=iphone_options[0]['id'], discount=welcome)
    second = order(seoyeon, chair, '박서연', '010-3456-7890', members[2][4], quantity=2)
    order(doyun, switch, '이도윤', '010-2345-6789', members[1][4])
    order(minjun, dyson, '최민준', '010-4567-8901', members[3][4], pay=False)
    order(minjun, macbook, '최민준', '010-4567-8901', members[3][4], discount=welcome)
    admin.put(f'/api/admin/orders/{first}/status', {'status': 'SHIPPING'})
    admin.put(f'/api/admin/orders/{first}/status', {'status': 'DELIVERED'})
    admin.put(f'/api/admin/orders/{second}/status', {'status': 'SHIPPING'})

    sky.create(f'/api/products/{iphone}/reviews', {'rating': 5, 'title': '검수 리포트 그대로입니다',
                                                   'contentHtml': p('배터리 94% 맞고 외관도 깨끗해요. 다음에도 여기서 살게요.')})
    seoyeon.create(f'/api/products/{chair}/reviews', {'rating': 4, 'title': '가볍고 튼튼해요',
                                                      'contentHtml': p('파우치에 작은 얼룩이 있었지만 의자는 새것 같아요.')})
    doyun.create(f'/api/products/{switch}/reviews', {'rating': 5, 'title': '아이랑 잘 쓰고 있어요',
                                                     'contentHtml': p('특가로 저렴하게 샀습니다. 조이콘 상태 최고!')})

    program_specs = [
        ('주말 플리마켓 셀러 모집 (성수)', '성수 픽업라운지 앞마당에서 열리는 주말 플리마켓 판매 부스', 20000, 30, 12, 14, 'flea-market.jpg',
         p('부스 1칸(2m×2m)과 테이블 1개를 제공합니다.', '당일 판매 수수료는 없습니다.')),
        ('중고 노트북 셀프 점검 클래스', '검수사에게 배우는 중고 노트북 점검법 (2시간)', 15000, 12, 9, 10, 'laptop-repair.jpg',
         p('배터리·디스플레이·키보드·포트 점검을 실습합니다.', '개인 노트북을 지참하면 1:1로 점검해 드립니다.')),
        ('캠핑용품 업사이클링 워크숍 (대전)', '낡은 캠핑 의자를 새 원단으로 리폼', 35000, 8, 20, 21, 'camping-chair.jpg',
         p('재료비가 포함되어 있습니다. 리폼할 의자를 가져오시면 됩니다.')),
    ]
    programs = []
    for name, description, price, capacity, start, end, image, body in program_specs:
        programs.append(admin.create('/api/admin/programs', {
            'name': name, 'description': description, 'price': price, 'maximumPerson': capacity,
            'imageFileId': admin.upload(image, name),
            'startDateTime': at(start, 11), 'endDateTime': at(end, 17),
            'applicationStartDateTime': at(-7, 9), 'applicationEndDateTime': at(start - 2, 18),
            'contentHtml': body, 'isEnabled': True,
        }))
    flea_market, laptop_class, _ = programs

    def reserve(client, program, name, phone, quantity=1, pay=True):
        created = client.post('/api/reservations', {'programId': program, 'quantity': quantity,
                                                    'applicantName': name, 'phoneNumber': phone})
        if pay:
            client.post('/api/payments', {'targetType': 'RESERVATION', 'targetId': created['id'],
                                          'amount': created['totalPrice'], 'method': 'BANK_TRANSFER'})
        return created['id']

    reserve(doyun, flea_market, '이도윤', '010-2345-6789')
    reserve(minjun, laptop_class, '최민준', '010-4567-8901')
    reserve(sky, laptop_class, '김하늘', '010-1234-5678', quantity=2)
    reserve(seoyeon, flea_market, '박서연', '010-3456-7890', pay=False)
    minjun.create(f'/api/programs/{laptop_class}/reviews', {'rating': 5, 'title': '신청 전부터 기대돼요',
                                                            'contentHtml': p('사전 안내 메일이 친절했어요. 노트북 챙겨 갑니다!')})

    laptop_video = admin.create('/api/admin/media', {
        'title': '[강의] 중고 노트북 셀프 점검 기초', 'durationSeconds': 80, 'completionRate': 90, 'isUsing': True,
        'description': '중고천국 검수사가 알려 드리는 노트북 점검 4단계(배터리·디스플레이·키보드·포트)입니다.\n'
                       '"중고 노트북 셀프 점검 클래스" 수강 전에 미리 보면 좋아요.',
        'fileId': admin.upload('laptop-check-basics.mp4', '중고 노트북 셀프 점검 기초 강의', MEDIA),
        'thumbnailFileId': admin.upload('laptop-check-basics.jpg', '중고 노트북 셀프 점검 기초 썸네일', MEDIA),
    })
    for client, watched in [(sky, 80), (minjun, 32)]:
        client.post(f'/api/media/{laptop_video}/start')
        client.post(f'/api/media/{laptop_video}/progress', {'ranges': [{'start': 0, 'end': watched}], 'position': watched})

    questions = [
        (sky, {'title': '검수 기간은 얼마나 걸리나요?', 'contentHtml': p('판매하려고 하는데 검수센터 도착 후 며칠이면 판매 등록되나요?')},
         '<p>검수센터 도착 후 영업일 기준 1~2일 안에 검수를 마치고 바로 판매 등록됩니다.</p>'),
        (minjun, {'title': '수령 후 하자를 발견했어요', 'contentHtml': p('맥북 키 하나가 살짝 뻑뻑합니다. 환불 가능할까요?'), 'isSecret': True},
         '<p>불편을 드려 죄송합니다. 수령 후 7일 이내라 무료 반품이 가능합니다. 마이페이지 주문 내역에서 반품을 신청해 주세요.</p>'),
        (None, {'title': '비회원도 구매할 수 있나요?', 'contentHtml': p('가입 없이 한 번만 사보고 싶어요.'),
                'authorName': '궁금이', 'password': 'guest1234', 'email': 'guest@example.com'}, None),
    ]
    for client, body, answer in questions:
        question = (client or Client(base_url, 'site')).create('/api/questions', body)
        if answer:
            admin.put(f'/api/admin/questions/{question}/answer', {'contentHtml': answer})

    report = doyun.create('/api/reports', {'title': '외부 메신저로 선입금 요구', 'reporterName': '이도윤', 'email': 'doyun@example.com',
                                           'phoneNumber': '010-2345-6789',
                                           'contentHtml': p('자유게시판에서 연락 온 사람이 카카오톡으로 옮겨 계좌이체를 요구했습니다.')})
    admin.put(f'/api/admin/reports/{report}/status', {'status': 'IN_PROGRESS'})
    admin.create(f'/api/admin/reports/{report}/answers', {'title': '신고 접수 및 조치 안내',
                                                          'contentHtml': p('해당 계정을 이용 정지하고 수사기관 협조 요청을 진행 중입니다. 신고해 주셔서 감사합니다.')})
    Client(base_url, 'site').create('/api/reports', {'title': '가품 의심 상품', 'reporterName': '익명', 'password': 'guest1234',
                                                     'contentHtml': p('자유게시판에 올라온 명품 지갑 사진이 가품 같습니다.')})

    for client, score, opinion in [(sky, 5, '안전거래 설명이 이해하기 쉬웠어요'), (minjun, 4, '체크리스트를 인쇄용으로도 주세요'),
                                   (seoyeon, 5, '')]:
        client.post('/api/satisfactions', {'menuId': about_menu, 'score': score, 'opinion': opinion})

    admin.create('/api/admin/banners', {'type': 'BANNER', 'order': 1, 'name': '오픈 이벤트 띠배너', 'isUsing': True,
                                        'description': '🎉 중고천국 정식 오픈! 첫 구매 10% 할인 쿠폰을 드려요', 'link': '/products',
                                        'backgroundColor': '#0F766E', 'textColor': '#FFFFFF', 'dismissDays': 1})
    admin.create('/api/admin/banners', {'type': 'BANNER', 'order': 2, 'name': '점검 안내 (대기)', 'isUsing': False,
                                        'description': '11/12(목) 02:00~05:00 정기 점검이 있습니다.',
                                        'backgroundColor': '#B45309', 'textColor': '#FFFFFF', 'dismissDays': 1})
    admin.create('/api/admin/banners', {'type': 'POPUP', 'order': 1, 'name': '플리마켓 셀러 모집', 'isUsing': True, 'isModal': True,
                                        'description': '성수 주말 플리마켓 셀러를 모집합니다. 부스비 2만 원, 판매 수수료 0원!',
                                        'fileId': admin.upload('flea-market.jpg', '플리마켓 셀러 모집'),
                                        'link': f'/programs/{flea_market}', 'width': 420, 'dismissDays': 3})
    admin.create('/api/admin/banners', {'type': 'POPUP', 'order': 2, 'name': '안전결제 안내', 'isUsing': True, 'isModal': True,
                                        'description': '외부 메신저·계좌이체 요구는 100% 사기입니다.\n반드시 중고천국 안전결제를 이용하세요.',
                                        'link': f'/contents/{guide}', 'width': 380, 'dismissDays': 7})

    print('중고천국 샘플 사이트를 만들었습니다.')
    print('  관리자: admin / (입력한 관리자 비밀번호)')
    print('  회원:   ' + ', '.join(username for username, *_ in members) + ' / junggo1234!')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument('--base-url', default='http://localhost:8080')
    parser.add_argument('--admin-password', default='admin1234!')
    args = parser.parse_args()
    seed(args.base_url.rstrip('/'), args.admin_password)

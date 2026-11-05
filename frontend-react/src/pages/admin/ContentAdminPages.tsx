import {useCallback, useEffect, useState} from 'react';
import {Link, useParams, useSearchParams} from 'react-router';
import type {Query} from '../../api/client';
import type {Archive, ArchiveArticleSummary, Board, Content, QuestionSummary} from '../../api/domain';
import {adminApi, archiveApi, qnaApi} from '../../api/endpoints';
import type {FileInfo} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {GuestPostingToggle} from '../../components/GuestPostingToggle';
import {HOME_BLOCKS} from '../../components/HomeBlocks';
import {formatDateTime} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {CONTENT_STATUS, PUBLISH_STATUS, toOptions} from '../../components/labels';
import {type Grade, gradeSelectField, useWithGradeFields} from '../../api/grades';
import {type FieldSpec, SchemaForm} from '../../components/SchemaForm';
import {StableLabel} from '../../components/StableLabel';

const BOARD_FIELDS: FieldSpec[] = [
    {name: 'type', label: '형식', type: 'select', required: true, options: [
            {value: 'NORMAL', label: '일반'}, {value: 'GALLERY', label: '갤러리'}]},
    {name: 'name', label: '게시판 이름', required: true, newRow: true},
    {name: 'description', label: '설명', wide: true},
    {name: 'pageSize', label: '페이지당 글 수', type: 'number', required: true},
    {name: 'maximumFileCount', label: '최대 첨부 수', type: 'number', required: true},
    {name: 'isMemberAccessible', label: '회원 목록 보기', type: 'checkbox', group: '기능'},
    {name: 'isMemberReadable', label: '회원 글 읽기', type: 'checkbox', group: '기능'},
    {name: 'isMemberWritable', label: '회원 글 쓰기', type: 'checkbox', group: '기능'},
    {name: 'isGuestAccessible', label: '비회원 목록 보기', type: 'checkbox', group: '기능'},
    {name: 'isGuestReadable', label: '비회원 글 읽기', type: 'checkbox', group: '기능'},
    {name: 'isGuestWritable', label: '비회원 글 쓰기', type: 'checkbox', group: '기능'},
    {name: 'supportsNotice', label: '공지 기능', type: 'checkbox', group: '기능'},
    {name: 'supportsSecret', label: '비밀글 기능', type: 'checkbox', group: '기능'},
    {name: 'supportsComment', label: '댓글 기능', type: 'checkbox', group: '기능'},
    {name: 'supportsLike', label: '추천 기능', type: 'checkbox', group: '기능'},
    {name: 'supportsAttachment', label: '첨부파일 기능', type: 'checkbox', group: '기능'},
    {name: 'supportsPaidContent', label: '유료 글(페이월) 기능', type: 'checkbox', group: '기능',
        help: '글마다 결제해야 볼 수 있는 유료 글로 지정할 수 있습니다.'},
];

const boardGradeFields = (grades: Grade[]) => [
    gradeSelectField('readGradeId', '글 읽기 등급', grades, '이 등급 이상 회원만 글을 읽습니다. 목록은 그대로 보입니다.'),
    gradeSelectField('writeGradeId', '글쓰기 등급', grades, '이 등급 이상 회원만 글을 씁니다. 지정하면 비회원은 쓸 수 없습니다.'),
];

const BOARD_DEFAULTS = {
    type: 'NORMAL', pageSize: 10, maximumFileCount: 5,
    isMemberAccessible: true, isMemberReadable: true, isMemberWritable: true,
    isGuestAccessible: true, isGuestReadable: true, isGuestWritable: false,
    supportsNotice: true, supportsComment: true,
};

const loadBoards = () => adminApi.boards.list();

export function BoardAdminPage() {
    const fields = useWithGradeFields(BOARD_FIELDS, boardGradeFields);
    return (
        <AdminCrudPage<Board>
            title="게시판"
            description={
                <>
                    <p><strong>방문자와 관리자가 함께 글을 올리는 공간</strong>에 씁니다. 예: 자유게시판, 거래 후기, 묻고 답하기.</p>
                    <p>회원·비회원별 읽기/쓰기 권한, 비밀글, 댓글, 추천, 첨부파일을 게시판마다 켜고 끌 수 있습니다.
                        관리자만 쓰는 공지·자료 게시는 아카이브가 더 알맞습니다.</p>
                </>
            }
            searchable={false}
            load={loadBoards}
            fields={fields}
            emptyForm={BOARD_DEFAULTS}
            columns={[
                {label: 'ID', render: (b) => b.id},
                {label: '이름', render: (b) => <Link to={`/boards/${b.id}`}>{b.name}</Link>},
                {label: '형식', render: (b) => (b.type === 'GALLERY' ? '갤러리' : '일반')},
                {label: '비회원 쓰기', render: (b) => <YesNo value={b.isGuestWritable}/>},
                {label: '댓글', render: (b) => <YesNo value={b.supportsComment}/>},
            ]}
            create={(v) => adminApi.boards.create(v)}
            update={(b, v) => adminApi.boards.update(b.id, v)}
            remove={(b) => adminApi.boards.remove(b.id)}
        />
    );
}

const CONTENT_FIELDS: FieldSpec[] = [
    {name: 'status', label: '저장 방식', type: 'select', required: true, options: toOptions(PUBLISH_STATUS),
        help: '보통은 임시 저장한 뒤 목록에서 검토를 요청합니다. 바로 게시는 최고 관리자만 할 수 있습니다. 검토 중인 글을 고치면 임시 저장으로 돌아갑니다.'},
    {name: 'title', label: '제목', required: true, newRow: true},
    {name: 'contentHtml', label: '내용', type: 'html'},
    {name: 'memo', label: '작업 메모', wide: true},
];

const contentGradeFields = (grades: Grade[]) => [
    gradeSelectField('accessGradeId', '열람 등급', grades, '이 등급 이상 회원만 볼 수 있습니다.'),
];

const loadContents = ({view, ...query}: Query) => (view === 'reviews' ? adminApi.contents.reviews(query) : adminApi.contents.search(query));

function ContentStatusCell({content}: { content: Content }) {
    return (
        <>
            <span className={content.status === 'REJECTED' ? 'badge danger' : content.status === 'PUBLISHED' ? 'badge ok' : 'badge'}>
                {CONTENT_STATUS[content.status]}
            </span>
            {content.status === 'IN_REVIEW' && <div className="muted small">{content.reviewRequestedBy} 요청 · {formatDateTime(content.reviewRequestedAt)}</div>}
            {content.publishAt && content.status !== 'PUBLISHED' && <div className="muted small">{formatDateTime(content.publishAt)} 게시 예정</div>}
            {content.status === 'REJECTED' && content.reviewComment && <div className="small">반려: {content.reviewComment}</div>}
            {content.status === 'PUBLISHED' && content.publishedAt && (
                <div className="muted small">{formatDateTime(content.publishedAt)}{content.reviewedBy && ` · ${content.reviewedBy} 승인`}</div>
            )}
        </>
    );
}

function ContentWorkflowActions({content, onDone}: { content: Content; onDone: () => void }) {
    const [publishAt, setPublishAt] = useState('');
    const [error, setError] = useState<unknown>(null);
    const run = (pending: Promise<unknown>) => {
        setError(null);
        pending.then(onDone).catch(setError);
    };
    return (
        <div className="review-cell">
            {(content.status === 'DRAFT' || content.status === 'REJECTED') && (
                <>
                    <input type="datetime-local" aria-label={`${content.title} 예약 발행 일시`} value={publishAt}
                           onChange={(e) => setPublishAt(e.target.value)}/>
                    <button type="button" onClick={() => run(adminApi.contents.requestReview(content.id, publishAt ? `${publishAt}:00` : null))}>
                        검토 요청
                    </button>
                </>
            )}
            {content.status === 'IN_REVIEW' && (
                <>
                    <button type="button" className="primary" onClick={() => run(adminApi.contents.approve(content.id))}>승인</button>
                    <button type="button" className="danger" onClick={() => {
                        const comment = window.prompt('반려 사유를 입력해 주세요.');
                        if (comment?.trim()) {
                            run(adminApi.contents.reject(content.id, comment.trim()));
                        }
                    }}>반려</button>
                </>
            )}
            {(content.status === 'PUBLISHED' || content.status === 'SCHEDULED') && (
                <button type="button" onClick={() => {
                    if (window.confirm(content.status === 'PUBLISHED' ? '게시를 내리고 임시 저장으로 돌릴까요?' : '게시 예약을 취소할까요?')) {
                        run(adminApi.contents.unpublish(content.id));
                    }
                }}>{content.status === 'PUBLISHED' ? '게시 취소' : '예약 취소'}</button>
            )}
            <ErrorBanner error={error}/>
        </div>
    );
}

export function ContentAdminPage() {
    const fields = useWithGradeFields(CONTENT_FIELDS, contentGradeFields);
    const [reviewOnly, setReviewOnly] = useState(false);
    return (
        <AdminCrudPage<Content>
            title="컨텐츠"
            description={
                <>
                    <p>게시된 컨텐츠는 /contents/{'{ID}'} 에서 보이며, 메뉴의 연결 대상으로 지정할 수 있습니다.</p>
                    <details>
                        <summary>컨텐츠로 홈 화면(랜딩 페이지) 꾸미기</summary>
                        <p>관리자 &gt; 사이트의 "홈 화면 컨텐츠"로 고른 컨텐츠가 홈에 그대로 표시됩니다. 사이트 테마의 CSS 클래스
                            (<code>hero</code>, <code>hero-description</code>, <code>chip-row</code>, <code>chip</code>,
                            <code>home-columns</code>, <code>home-section</code> 등)를 <code>class</code> 속성으로 쓸 수 있고,
                            아래처럼 빈 div 를 넣으면 그 자리에 실시간 내용이 채워집니다.</p>
                        <ul>
                            {Object.entries(HOME_BLOCKS).map(([name, block]) => (
                                <li key={name}><code>{`<div data-block="${name}"></div>`}</code> — {block.label}</li>
                            ))}
                        </ul>
                    </details>
                </>
            }
            load={loadContents}
            fields={fields}
            emptyForm={{status: 'DRAFT', contentHtml: ''}}
            extraQuery={reviewOnly ? {view: 'reviews'} : undefined}
            filters={
                <label className="checkbox">
                    <input type="checkbox" checked={reviewOnly} onChange={(e) => setReviewOnly(e.target.checked)}/> 검토 대기만 보기
                </label>
            }
            columns={[
                {label: 'ID', render: (c) => c.id},
                {label: '제목', render: (c) => c.status === 'PUBLISHED' ? <Link to={`/contents/${c.id}`}>{c.title}</Link> : c.title},
                {label: '상태', render: (c) => <ContentStatusCell content={c}/>},
                {label: '발행', className: 'button-cell', render: (c, reload) => <ContentWorkflowActions content={c} onDone={reload}/>},
                {label: '버전', render: (c) => `v${c.version}`},
                {label: '마지막 작업', render: (c) => `${c.lastWorker ?? '-'} · ${formatDateTime(c.modifiedAt)}`},
            ]}
            toForm={(c) => adminApi.contents.get(c.id).then((full) => ({...full, status: full.status === 'PUBLISHED' ? 'PUBLISHED' : 'DRAFT'}))}
            create={(v) => adminApi.contents.create(v)}
            update={(c, v) => adminApi.contents.update(c.id, v)}
            remove={(c) => adminApi.contents.remove(c.id)}
        />
    );
}

const ARCHIVE_FIELDS: FieldSpec[] = [
    {name: 'type', label: '형식', type: 'select', required: true, options: [
            {value: 'NORMAL', label: '일반'}, {value: 'GALLERY', label: '갤러리'}]},
    {name: 'name', label: '이름', required: true, newRow: true},
    {name: 'description', label: '설명', wide: true},
    {name: 'pageSize', label: '페이지당 글 수', type: 'number', required: true},
    {name: 'supportsComment', label: '회원 댓글 허용', type: 'checkbox', group: '기능'},
    {name: 'supportsLike', label: '추천', type: 'checkbox', group: '기능'},
];

const archiveGradeFields = (grades: Grade[]) => [
    gradeSelectField('accessGradeId', '글 읽기 등급', grades, '이 등급 이상 회원만 글을 읽습니다. 목록은 그대로 보입니다.'),
];

const loadArchives = () => adminApi.archives.list();

export function ArchiveAdminPage() {
    const fields = useWithGradeFields(ARCHIVE_FIELDS, archiveGradeFields);
    return (
        <AdminCrudPage<Archive>
            title="아카이브"
            description={
                <>
                    <p><strong>관리자만 글을 올리고 방문자는 읽기만 하는 공간</strong>에 씁니다. 예: 공지사항, 보도자료, 자료실.</p>
                    <p>글마다 초안/공개 상태를 두어 준비가 끝난 글만 공개할 수 있고, 필요하면 방문자 댓글만 열어 둘 수 있습니다.
                        방문자도 글을 써야 한다면 게시판을 쓰세요.</p>
                </>
            }
            searchable={false}
            load={loadArchives}
            fields={fields}
            emptyForm={{type: 'NORMAL', pageSize: 10, supportsComment: false, supportsLike: false}}
            columns={[
                {label: 'ID', render: (a) => a.id},
                {label: '이름', render: (a) => <Link to={`/admin/archives/${a.id}`}>{a.name}</Link>},
                {label: '형식', render: (a) => (a.type === 'GALLERY' ? '갤러리' : '일반')},
                {label: '댓글', render: (a) => <YesNo value={a.supportsComment}/>},
                {label: '추천', render: (a) => <YesNo value={a.supportsLike}/>},
            ]}
            create={(v) => adminApi.archives.create(v)}
            update={(a, v) => adminApi.archives.update(a.id, v)}
            remove={(a) => adminApi.archives.remove(a.id)}
        />
    );
}

const ARCHIVE_ARTICLE_FIELDS: FieldSpec[] = [
    {name: 'status', label: '상태', type: 'select', required: true, options: toOptions(PUBLISH_STATUS)},
    {name: 'title', label: '제목', required: true, newRow: true},
    {name: 'contentHtml', label: '내용', type: 'html'},
    {name: 'files', label: '첨부파일', type: 'files'},
];

const toArticleBody = (values: Record<string, unknown>) => ({
    ...values,
    fileIds: ((values.files as FileInfo[] | undefined) ?? []).map((file) => file.id),
});

export function ArchiveArticleAdminPage() {
    const archiveId = Number(useParams().id);
    const articles = adminApi.archives.articles(archiveId);
    const load = useCallback((query: Query) =>
        archiveApi.articles(archiveId, String(query.keyword ?? ''), Number(query.page ?? 0)), [archiveId]);

    return (
        <>
            <Link to="/admin/archives">← 아카이브 목록</Link>
            <AdminCrudPage<ArchiveArticleSummary>
                title={`아카이브 #${archiveId} 게시물`}
                load={load}
                fields={ARCHIVE_ARTICLE_FIELDS}
                emptyForm={{status: 'DRAFT', contentHtml: '', files: []}}
                columns={[
                    {label: '제목', render: (a) => <Link to={`/archives/${archiveId}/articles/${a.id}`}>{a.title}</Link>},
                    {label: '상태', render: (a) => PUBLISH_STATUS[a.status]},
                    {label: '조회', render: (a) => a.hit},
                    {label: '등록일', render: (a) => formatDateTime(a.createdAt)},
                ]}
                toForm={(a) => articles.get(a.id).then((full) => ({...full}))}
                create={(v) => articles.create(toArticleBody(v))}
                update={(a, v) => articles.update(a.id, toArticleBody(v))}
                remove={(a) => articles.remove(a.id)}
            />
        </>
    );
}

const loadQuestions = (query: Query) =>
    qnaApi.list(String(query.keyword ?? ''), Number(query.page ?? 0), query.unanswered === true);

export function QuestionAdminPage() {
    const [answering, setAnswering] = useState<number | null>(null);
    const [searchParams, setSearchParams] = useSearchParams();
    const unanswered = searchParams.get('unanswered') === 'true';
    return (
        <AdminCrudPage<QuestionSummary>
            title="질문답변"
            description={<GuestPostingToggle label="비회원도 질문 등록 허용" load={qnaApi.guestPosting} save={adminApi.questions.setGuestPosting}/>}
            load={loadQuestions}
            extraQuery={{unanswered}}
            filters={
                <label className="checkbox">
                    <input type="checkbox" checked={unanswered}
                           onChange={(e) => setSearchParams(e.target.checked ? {unanswered: 'true'} : {})}/>
                    미답변만 보기
                </label>
            }
            columns={[
                {label: '제목', render: (q) => <Link to={`/qna/${q.id}`}>{q.title}</Link>},
                {label: '작성자', render: (q) => q.authorName},
                {label: '비밀글', render: (q) => <YesNo value={q.isSecret}/>},
                {label: '답변', render: (q) => q.answered ? <span className="badge ok">완료</span> : <span className="badge danger">대기</span>},
                {label: '등록일', render: (q) => formatDateTime(q.createdAt)},
            ]}
            actions={(q) => (
                <button type="button" disabled={answering === q.id} onClick={() => setAnswering(q.id)}>
                    <StableLabel current={q.answered ? '답변 수정' : '답변하기'} labels={['답변 수정', '답변하기']}/>
                </button>
            )}
            expandedRow={(q, reload) => answering === q.id && (
                <AnswerEditor questionId={q.id} onDone={() => {
                    setAnswering(null);
                    reload();
                }}/>
            )}
        />
    );
}

function AnswerEditor({questionId, onDone}: { questionId: number; onDone: () => void }) {
    const [error, setError] = useState<unknown>(null);
    const [question, setQuestion] = useState<Awaited<ReturnType<typeof qnaApi.get>> | null>(null);

    useEffect(() => {
        qnaApi.get(questionId).then(setQuestion).catch(setError);
    }, [questionId]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!question) {
        return <span className="muted">불러오는 중…</span>;
    }
    return (
        <div>
            <HtmlView html={question.contentHtml}/>
            <SchemaForm
                fields={[{name: 'contentHtml', label: '답변', type: 'html', required: true}]}
                initial={{contentHtml: question.answer?.contentHtml ?? ''}}
                submitLabel="답변 저장"
                onCancel={onDone}
                onSubmit={async (values) => {
                    await adminApi.questions.answer(questionId, String(values.contentHtml ?? ''));
                    onDone();
                }}
            />
        </div>
    );
}

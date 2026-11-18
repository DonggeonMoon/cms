import {useCallback, useEffect, useState} from 'react';
import {Link, useParams, useSearchParams} from 'react-router';
import type {Query} from '../../api/client';
import type {Archive, ArchiveArticleSummary, Board, Content, QuestionSummary} from '../../api/domain';
import {adminApi, archiveApi, qnaApi} from '../../api/endpoints';
import type {FileInfo} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {GuestPostingToggle} from '../../components/GuestPostingToggle';
import {homeBlocks} from '../../components/HomeBlocks';
import {formatDateTime} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {contentStatus, publishStatus, toOptions} from '../../components/labels';
import {type Grade, gradeSelectField, useWithGradeFields} from '../../api/grades';
import {useI18n} from '../../api/i18n';
import {type FieldSpec, SchemaForm} from '../../components/SchemaForm';
import {StableLabel} from '../../components/StableLabel';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const boardFields = (t: T): FieldSpec[] => [
    {name: 'type', label: t('admin.board.field.type', '형식'), type: 'select', required: true, options: [
            {value: 'NORMAL', label: t('admin.board.type.normal', '일반')}, {value: 'GALLERY', label: t('admin.board.type.gallery', '갤러리')}]},
    {name: 'name', label: t('admin.board.field.name', '게시판 이름'), required: true, newRow: true},
    {name: 'description', label: t('admin.board.field.description', '설명'), wide: true},
    {name: 'pageSize', label: t('admin.board.field.pageSize', '페이지당 글 수'), type: 'number', min: 0, required: true},
    {name: 'maximumFileCount', label: t('admin.board.field.maximumFileCount', '최대 첨부 수'), type: 'number', min: 0, required: true},
    {name: 'isMemberAccessible', label: t('admin.board.field.isMemberAccessible', '회원 목록 보기'), type: 'checkbox', group: t('admin.board.group.features', '기능'), subgroup: t('admin.board.subgroup.member', '회원 기능')},
    {name: 'isMemberReadable', label: t('admin.board.field.isMemberReadable', '회원 글 읽기'), type: 'checkbox', group: t('admin.board.group.features', '기능'), subgroup: t('admin.board.subgroup.member', '회원 기능')},
    {name: 'isMemberWritable', label: t('admin.board.field.isMemberWritable', '회원 글 쓰기'), type: 'checkbox', group: t('admin.board.group.features', '기능'), subgroup: t('admin.board.subgroup.member', '회원 기능')},
    {name: 'isGuestAccessible', label: t('admin.board.field.isGuestAccessible', '비회원 목록 보기'), type: 'checkbox', group: t('admin.board.group.features', '기능'), subgroup: t('admin.board.subgroup.guest', '비회원 기능')},
    {name: 'isGuestReadable', label: t('admin.board.field.isGuestReadable', '비회원 글 읽기'), type: 'checkbox', group: t('admin.board.group.features', '기능'), subgroup: t('admin.board.subgroup.guest', '비회원 기능')},
    {name: 'isGuestWritable', label: t('admin.board.field.isGuestWritable', '비회원 글 쓰기'), type: 'checkbox', group: t('admin.board.group.features', '기능'), subgroup: t('admin.board.subgroup.guest', '비회원 기능')},
    {name: 'supportsNotice', label: t('admin.board.field.supportsNotice', '공지 기능'), type: 'checkbox', group: t('admin.board.group.features', '기능')},
    {name: 'supportsSecret', label: t('admin.board.field.supportsSecret', '비밀글 기능'), type: 'checkbox', group: t('admin.board.group.features', '기능')},
    {name: 'supportsComment', label: t('admin.board.field.supportsComment', '댓글 기능'), type: 'checkbox', group: t('admin.board.group.features', '기능')},
    {name: 'supportsLike', label: t('admin.board.field.supportsLike', '추천 기능'), type: 'checkbox', group: t('admin.board.group.features', '기능')},
    {name: 'supportsAttachment', label: t('admin.board.field.supportsAttachment', '첨부파일 기능'), type: 'checkbox', group: t('admin.board.group.features', '기능')},
    {name: 'supportsPaidContent', label: t('admin.board.field.supportsPaidContent', '유료 글(페이월) 기능'), type: 'checkbox', group: t('admin.board.group.features', '기능'),
        help: t('admin.board.field.supportsPaidContent.help', '글마다 결제해야 볼 수 있는 유료 글로 지정할 수 있습니다.')},
];

const boardGradeFields = (grades: Grade[], t: T) => [
    gradeSelectField('readGradeId', t('admin.board.field.readGradeId', '글 읽기 등급'), grades, t('admin.board.field.readGradeId.help', '이 등급 이상 회원만 글을 읽습니다. 목록은 그대로 보입니다.'), t),
    gradeSelectField('writeGradeId', t('admin.board.field.writeGradeId', '글쓰기 등급'), grades, t('admin.board.field.writeGradeId.help', '이 등급 이상 회원만 글을 씁니다. 지정하면 비회원은 쓸 수 없습니다.'), t),
];

const BOARD_DEFAULTS = {
    type: 'NORMAL', pageSize: 10, maximumFileCount: 5,
    isMemberAccessible: true, isMemberReadable: true, isMemberWritable: true,
    isGuestAccessible: true, isGuestReadable: true, isGuestWritable: false,
    supportsNotice: true, supportsComment: true,
};

const loadBoards = () => adminApi.boards.list();

export function BoardAdminPage() {
    const {t} = useI18n();
    const fields = useWithGradeFields(boardFields(t), (grades) => boardGradeFields(grades, t));
    return (
        <AdminCrudPage<Board>
            title={t('admin.board.title', '게시판')}
            description={
                <>
                    <p>{t('admin.board.description1', '방문자와 관리자가 함께 글을 올리는 공간에 씁니다. 예: 자유게시판, 거래 후기, 묻고 답하기.')}</p>
                    <p>{t('admin.board.description2', '회원·비회원별 읽기/쓰기 권한, 비밀글, 댓글, 추천, 첨부파일을 게시판마다 켜고 끌 수 있습니다. 관리자만 쓰는 공지·자료 게시는 아카이브가 더 알맞습니다.')}</p>
                </>
            }
            searchable={false}
            load={loadBoards}
            fields={fields}
            emptyForm={BOARD_DEFAULTS}
            columns={[
                {label: 'ID', render: (b) => b.id},
                {label: t('admin.board.column.name', '이름'), render: (b) => <Link to={`/boards/${b.id}`}>{b.name}</Link>},
                {label: t('admin.board.field.type', '형식'), render: (b) => (b.type === 'GALLERY' ? t('admin.board.type.gallery', '갤러리') : t('admin.board.type.normal', '일반'))},
                {label: t('admin.board.column.guestWritable', '비회원 쓰기'), render: (b) => <YesNo value={b.isGuestWritable}/>},
                {label: t('admin.board.field.supportsComment', '댓글'), render: (b) => <YesNo value={b.supportsComment}/>},
            ]}
            create={(v) => adminApi.boards.create(v)}
            update={(b, v) => adminApi.boards.update(b.id, v)}
            remove={(b) => adminApi.boards.remove(b.id)}
        />
    );
}

const contentFields = (t: T): FieldSpec[] => [
    {name: 'status', label: t('admin.content.field.status', '저장 방식'), type: 'select', required: true, options: toOptions(publishStatus(t)),
        help: t('admin.content.field.status.help', '보통은 임시 저장한 뒤 목록에서 검토를 요청합니다. 바로 게시는 최고 관리자만 할 수 있습니다. 검토 중인 글을 고치면 임시 저장으로 돌아갑니다.')},
    {name: 'title', label: t('admin.content.field.title', '제목'), required: true, newRow: true},
    {name: 'contentHtml', label: t('admin.content.field.contentHtml', '내용'), type: 'html'},
    {name: 'memo', label: t('admin.content.field.memo', '작업 메모'), wide: true},
];

const contentGradeFields = (grades: Grade[], t: T) => [
    gradeSelectField('accessGradeId', t('admin.content.field.accessGradeId', '열람 등급'), grades, t('admin.content.field.accessGradeId.help', '이 등급 이상 회원만 볼 수 있습니다.'), t),
];

const loadContents = ({view, ...query}: Query) => (view === 'reviews' ? adminApi.contents.reviews(query) : adminApi.contents.search(query));

function ContentStatusCell({content}: { content: Content }) {
    const {t} = useI18n();
    return (
        <>
            <span className={content.status === 'REJECTED' ? 'badge danger' : content.status === 'PUBLISHED' ? 'badge ok' : 'badge'}>
                {contentStatus(t)[content.status]}
            </span>
            {content.status === 'IN_REVIEW' && <div className="muted small">{t('admin.content.reviewRequested', '{by} 요청 · {at}', {by: content.reviewRequestedBy ?? '', at: formatDateTime(content.reviewRequestedAt)})}</div>}
            {content.publishAt && content.status !== 'PUBLISHED' && <div className="muted small">{t('admin.content.scheduledPublish', '{date} 게시 예정', {date: formatDateTime(content.publishAt)})}</div>}
            {content.status === 'REJECTED' && content.reviewComment && <div className="small">{t('admin.content.rejected', '반려: {comment}', {comment: content.reviewComment})}</div>}
            {content.status === 'PUBLISHED' && content.publishedAt && (
                <div className="muted small">{formatDateTime(content.publishedAt)}{content.reviewedBy && t('admin.content.approvedBy', ' · {by} 승인', {by: content.reviewedBy})}</div>
            )}
        </>
    );
}

function ContentWorkflowActions({content, onDone}: { content: Content; onDone: () => void }) {
    const {t} = useI18n();
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
                    <input type="datetime-local" aria-label={t('admin.content.scheduledPublishAt', '{title} 예약 발행 일시', {title: content.title})} value={publishAt}
                           onChange={(e) => setPublishAt(e.target.value)}/>
                    <button type="button" onClick={() => run(adminApi.contents.requestReview(content.id, publishAt ? `${publishAt}:00` : null))}>
                        {t('admin.content.requestReview', '검토 요청')}
                    </button>
                </>
            )}
            {content.status === 'IN_REVIEW' && (
                <>
                    <button type="button" className="primary" onClick={() => run(adminApi.contents.approve(content.id))}>{t('admin.orderReturn.approve', '승인')}</button>
                    <button type="button" className="danger" onClick={() => {
                        const comment = window.prompt(t('admin.content.rejectPrompt', '반려 사유를 입력해 주세요.'));
                        if (comment?.trim()) {
                            run(adminApi.contents.reject(content.id, comment.trim()));
                        }
                    }}>{t('admin.course.decline', '반려')}</button>
                </>
            )}
            {(content.status === 'PUBLISHED' || content.status === 'SCHEDULED') && (
                <button type="button" onClick={() => {
                    if (window.confirm(content.status === 'PUBLISHED' ? t('admin.content.confirmUnpublish', '게시를 내리고 임시 저장으로 돌릴까요?') : t('admin.content.confirmCancelSchedule', '게시 예약을 취소할까요?'))) {
                        run(adminApi.contents.unpublish(content.id));
                    }
                }}>{content.status === 'PUBLISHED' ? t('admin.content.unpublish', '게시 취소') : t('admin.content.cancelSchedule', '예약 취소')}</button>
            )}
            <ErrorBanner error={error}/>
        </div>
    );
}

export function ContentAdminPage() {
    const {t} = useI18n();
    const fields = useWithGradeFields(contentFields(t), (grades) => contentGradeFields(grades, t));
    const [reviewOnly, setReviewOnly] = useState(false);
    return (
        <AdminCrudPage<Content>
            title={t('admin.nav.contents', '컨텐츠')}
            description={
                <>
                    <p>{t('admin.content.description', '게시된 컨텐츠는 /contents/{ID} 에서 보이며, 메뉴의 연결 대상으로 지정할 수 있습니다.')}</p>
                    <details>
                        <summary>{t('admin.content.homeDecorating', '컨텐츠로 홈 화면(랜딩 페이지) 꾸미기')}</summary>
                        <p>{t('admin.content.homeDecoratingHelp', '관리자 > 사이트의 "홈 화면 컨텐츠"로 고른 컨텐츠가 홈에 그대로 표시됩니다. 사이트 테마의 CSS 클래스(hero, hero-description, chip-row, chip, home-columns, home-section 등)를 class 속성으로 쓸 수 있고, 아래처럼 빈 div 를 넣으면 그 자리에 실시간 내용이 채워집니다.')}</p>
                        <ul>
                            {Object.entries(homeBlocks(t)).map(([name, block]) => (
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
                    <input type="checkbox" checked={reviewOnly} onChange={(e) => setReviewOnly(e.target.checked)}/> {t('admin.content.reviewOnly', '검토 대기만 보기')}
                </label>
            }
            columns={[
                {label: 'ID', render: (c) => c.id},
                {label: t('admin.content.field.title', '제목'), render: (c) => c.status === 'PUBLISHED' ? <Link to={`/contents/${c.id}`}>{c.title}</Link> : c.title},
                {label: t('admin.order.column.status', '상태'), render: (c) => <ContentStatusCell content={c}/>},
                {label: t('admin.content.column.publish', '발행'), className: 'button-cell', render: (c, reload) => <ContentWorkflowActions content={c} onDone={reload}/>},
                {label: t('admin.content.column.version', '버전'), render: (c) => `v${c.version}`},
                {label: t('admin.content.column.lastWorker', '마지막 작업'), render: (c) => `${c.lastWorker ?? '-'} · ${formatDateTime(c.modifiedAt)}`},
            ]}
            toForm={(c) => adminApi.contents.get(c.id).then((full) => ({...full, status: full.status === 'PUBLISHED' ? 'PUBLISHED' : 'DRAFT'}))}
            create={(v) => adminApi.contents.create(v)}
            update={(c, v) => adminApi.contents.update(c.id, v)}
            remove={(c) => adminApi.contents.remove(c.id)}
        />
    );
}

const archiveFields = (t: T): FieldSpec[] => [
    {name: 'type', label: t('admin.board.field.type', '형식'), type: 'select', required: true, options: [
            {value: 'NORMAL', label: t('admin.board.type.normal', '일반')}, {value: 'GALLERY', label: t('admin.board.type.gallery', '갤러리')}]},
    {name: 'name', label: t('admin.archive.field.name', '이름'), required: true, newRow: true},
    {name: 'description', label: t('admin.board.field.description', '설명'), wide: true},
    {name: 'pageSize', label: t('admin.board.field.pageSize', '페이지당 글 수'), type: 'number', min: 0, required: true},
    {name: 'supportsComment', label: t('admin.archive.field.supportsComment', '댓글 허용'), type: 'checkbox', group: t('admin.board.group.features', '기능')},
    {name: 'isGuestCommentable', label: t('admin.archive.field.isGuestCommentable', '비회원 댓글 허용'), type: 'checkbox', group: t('admin.board.group.features', '기능'),
        help: t('admin.archive.field.isGuestCommentable.help', '켜면 로그인하지 않은 방문자도 이름·비밀번호를 입력해 댓글을 남길 수 있습니다.')},
    {name: 'supportsLike', label: t('admin.archive.field.supportsLike', '추천'), type: 'checkbox', group: t('admin.board.group.features', '기능')},
];

const archiveGradeFields = (grades: Grade[], t: T) => [
    gradeSelectField('accessGradeId', t('admin.board.field.readGradeId', '글 읽기 등급'), grades, t('admin.board.field.readGradeId.help', '이 등급 이상 회원만 글을 읽습니다. 목록은 그대로 보입니다.'), t),
];

const loadArchives = () => adminApi.archives.list();

export function ArchiveAdminPage() {
    const {t} = useI18n();
    const fields = useWithGradeFields(archiveFields(t), (grades) => archiveGradeFields(grades, t));
    return (
        <AdminCrudPage<Archive>
            title={t('admin.nav.archives', '아카이브')}
            description={
                <>
                    <p>{t('admin.archive.description1', '관리자만 글을 올리고 방문자는 읽기만 하는 공간에 씁니다. 예: 공지사항, 보도자료, 자료실.')}</p>
                    <p>{t('admin.archive.description2', '글마다 초안/공개 상태를 두어 준비가 끝난 글만 공개할 수 있고, 필요하면 방문자 댓글만 열어 둘 수 있습니다. 방문자도 글을 써야 한다면 게시판을 쓰세요.')}</p>
                </>
            }
            searchable={false}
            load={loadArchives}
            fields={fields}
            emptyForm={{type: 'NORMAL', pageSize: 10, supportsComment: false, supportsLike: false}}
            columns={[
                {label: 'ID', render: (a) => a.id},
                {label: t('admin.board.column.name', '이름'), render: (a) => <Link to={`/admin/archives/${a.id}`}>{a.name}</Link>},
                {label: t('admin.board.field.type', '형식'), render: (a) => (a.type === 'GALLERY' ? t('admin.board.type.gallery', '갤러리') : t('admin.board.type.normal', '일반'))},
                {label: t('admin.board.field.supportsComment', '댓글'), render: (a) => <YesNo value={a.supportsComment}/>},
                {label: t('admin.archive.field.supportsLike', '추천'), render: (a) => <YesNo value={a.supportsLike}/>},
            ]}
            create={(v) => adminApi.archives.create(v)}
            update={(a, v) => adminApi.archives.update(a.id, v)}
            remove={(a) => adminApi.archives.remove(a.id)}
        />
    );
}

const archiveArticleFields = (t: T): FieldSpec[] => [
    {name: 'status', label: t('admin.order.column.status', '상태'), type: 'select', required: true, options: toOptions(publishStatus(t))},
    {name: 'title', label: t('admin.content.field.title', '제목'), required: true, newRow: true},
    {name: 'contentHtml', label: t('admin.content.field.contentHtml', '내용'), type: 'html'},
    {name: 'files', label: t('admin.archive.field.files', '첨부파일'), type: 'files'},
];

const toArticleBody = (values: Record<string, unknown>) => ({
    ...values,
    fileIds: ((values.files as FileInfo[] | undefined) ?? []).map((file) => file.id),
});

export function ArchiveArticleAdminPage() {
    const {t} = useI18n();
    const archiveId = Number(useParams().id);
    const articles = adminApi.archives.articles(archiveId);
    const load = useCallback((query: Query) =>
        archiveApi.articles(archiveId, String(query.keyword ?? ''), Number(query.page ?? 0)), [archiveId]);

    return (
        <>
            <Link to="/admin/archives">{t('admin.archive.backToList', '← 아카이브 목록')}</Link>
            <AdminCrudPage<ArchiveArticleSummary>
                title={t('admin.archive.articlesTitle', '아카이브 #{id} 게시물', {id: String(archiveId)})}
                load={load}
                fields={archiveArticleFields(t)}
                emptyForm={{status: 'DRAFT', contentHtml: '', files: []}}
                columns={[
                    {label: t('admin.content.field.title', '제목'), render: (a) => <Link to={`/archives/${archiveId}/articles/${a.id}`}>{a.title}</Link>},
                    {label: t('admin.order.column.status', '상태'), render: (a) => publishStatus(t)[a.status]},
                    {label: t('admin.archive.column.hit', '조회'), render: (a) => a.hit},
                    {label: t('admin.archive.column.createdAt', '등록일'), render: (a) => formatDateTime(a.createdAt)},
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
    const {t} = useI18n();
    const [answering, setAnswering] = useState<number | null>(null);
    const [searchParams, setSearchParams] = useSearchParams();
    const unanswered = searchParams.get('unanswered') === 'true';
    return (
        <AdminCrudPage<QuestionSummary>
            title={t('admin.nav.questions', '질문답변')}
            description={<GuestPostingToggle label={t('admin.question.guestPosting', '비회원도 질문 등록 허용')} load={qnaApi.guestPosting} save={adminApi.questions.setGuestPosting}/>}
            load={loadQuestions}
            extraQuery={{unanswered}}
            filters={
                <label className="checkbox">
                    <input type="checkbox" checked={unanswered}
                           onChange={(e) => setSearchParams(e.target.checked ? {unanswered: 'true'} : {})}/>
                    {t('admin.question.unansweredOnly', '미답변만 보기')}
                </label>
            }
            columns={[
                {label: t('admin.content.field.title', '제목'), render: (q) => <Link to={`/qna/${q.id}`}>{q.title}</Link>},
                {label: t('admin.order.column.receiver', '작성자'), render: (q) => q.authorName},
                {label: t('admin.board.field.supportsSecret', '비밀글'), render: (q) => <YesNo value={q.isSecret}/>},
                {label: t('admin.question.answer', '답변'), render: (q) => q.answered ? <span className="badge ok">{t('admin.question.done', '완료')}</span> : <span className="badge danger">{t('admin.question.waiting', '대기')}</span>},
                {label: t('admin.archive.column.createdAt', '등록일'), render: (q) => formatDateTime(q.createdAt)},
            ]}
            actions={(q) => (
                <button type="button" disabled={answering === q.id} onClick={() => setAnswering(q.id)}>
                    <StableLabel current={q.answered ? t('admin.question.editAnswer', '답변 수정') : t('admin.question.writeAnswer', '답변하기')}
                                 labels={[t('admin.question.editAnswer', '답변 수정'), t('admin.question.writeAnswer', '답변하기')]}/>
                </button>
            )}
            expandedRow={(q, reload) => answering === q.id && (
                <AnswerEditor questionId={q.id} onDone={() => {
                    setAnswering(null);
                    reload();
                }}/>
            )}
            remove={(q) => qnaApi.remove(q.id)}
        />
    );
}

function AnswerEditor({questionId, onDone}: { questionId: number; onDone: () => void }) {
    const {t} = useI18n();
    const [error, setError] = useState<unknown>(null);
    const [question, setQuestion] = useState<Awaited<ReturnType<typeof qnaApi.get>> | null>(null);

    useEffect(() => {
        qnaApi.get(questionId).then(setQuestion).catch(setError);
    }, [questionId]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!question) {
        return <span className="muted">{t('common.loading', '불러오는 중…')}</span>;
    }
    return (
        <div>
            <HtmlView html={question.contentHtml}/>
            <SchemaForm
                fields={[{name: 'contentHtml', label: t('admin.question.answer', '답변'), type: 'html', required: true}]}
                initial={{contentHtml: question.answer?.contentHtml ?? ''}}
                submitLabel={t('admin.question.saveAnswer', '답변 저장')}
                onCancel={onDone}
                footer={question.answer && (
                    <button type="button" className="danger" onClick={async () => {
                        if (!window.confirm(t('admin.question.confirmDeleteAnswer', '답변을 삭제할까요?'))) {
                            return;
                        }
                        await adminApi.questions.deleteAnswer(questionId);
                        onDone();
                    }}>{t('admin.question.deleteAnswer', '답변 삭제')}</button>
                )}
                onSubmit={async (values) => {
                    await adminApi.questions.answer(questionId, String(values.contentHtml ?? ''));
                    onDone();
                }}
            />
        </div>
    );
}

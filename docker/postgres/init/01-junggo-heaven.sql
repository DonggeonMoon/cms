--
-- PostgreSQL database dump
--

\restrict LX6nUNWpJfiMLX8trQYu4rDaAwDpc68PI33Lrcgz2gyUnFewly6Ot6wRnXrrsYv

-- Dumped from database version 18.6 (Debian 18.6-1.pgdg13+2)
-- Dumped by pg_dump version 18.6 (Debian 18.6-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--



--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--



SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: admin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admin (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    birth_date date,
    email character varying(255),
    first_name character varying(255),
    gender character varying(255),
    group_id bigint,
    is_lunar_birth_date boolean DEFAULT false NOT NULL,
    last_login_date_time timestamp(6) without time zone,
    last_name character varying(255),
    login_try_count integer DEFAULT 0 NOT NULL,
    mobile_phone_name character varying(255),
    name character varying(255),
    password character varying(255) NOT NULL,
    role_id bigint,
    telephone_name character varying(255),
    term_confirmation_date date,
    term_expiration_date date,
    username character varying(255) NOT NULL
);


--
-- Name: COLUMN admin.birth_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.birth_date IS '생년월일';


--
-- Name: COLUMN admin.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.email IS '이메일';


--
-- Name: COLUMN admin.first_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.first_name IS '이름만';


--
-- Name: COLUMN admin.gender; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.gender IS '성별';


--
-- Name: COLUMN admin.group_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.group_id IS '그룹';


--
-- Name: COLUMN admin.is_lunar_birth_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.is_lunar_birth_date IS '음력 여부';


--
-- Name: COLUMN admin.last_login_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.last_login_date_time IS '마지막 로그인 일시';


--
-- Name: COLUMN admin.last_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.last_name IS '성';


--
-- Name: COLUMN admin.login_try_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.login_try_count IS '로그인 시도 횟수';


--
-- Name: COLUMN admin.mobile_phone_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.mobile_phone_name IS '휴대폰 번호';


--
-- Name: COLUMN admin.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.name IS '이름';


--
-- Name: COLUMN admin.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.password IS '비밀번호';


--
-- Name: COLUMN admin.role_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.role_id IS '권한';


--
-- Name: COLUMN admin.telephone_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.telephone_name IS '전화번호';


--
-- Name: COLUMN admin.term_confirmation_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.term_confirmation_date IS '약관 동의 확인 일시';


--
-- Name: COLUMN admin.term_expiration_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.term_expiration_date IS '약관 동의 만료 일시';


--
-- Name: COLUMN admin.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.username IS '관리자 ID';


--
-- Name: admin_address; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admin_address (
    id bigint NOT NULL,
    address character varying(255),
    admin_id bigint NOT NULL,
    detail_address character varying(255),
    postal_code character varying(255)
);


--
-- Name: COLUMN admin_address.address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_address.address IS '주소';


--
-- Name: COLUMN admin_address.admin_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_address.admin_id IS '관리자 ID';


--
-- Name: COLUMN admin_address.detail_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_address.detail_address IS '상세주소';


--
-- Name: COLUMN admin_address.postal_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_address.postal_code IS '우편번호';


--
-- Name: admin_address_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.admin_address ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.admin_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: admin_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.admin ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.admin_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: admin_organization; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admin_organization (
    id bigint NOT NULL,
    member_id bigint NOT NULL,
    office_address character varying(255),
    office_department character varying(255),
    office_detail_address character varying(255),
    office_name character varying(255),
    office_position character varying(255)
);


--
-- Name: COLUMN admin_organization.member_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_organization.member_id IS '관리자 ID';


--
-- Name: COLUMN admin_organization.office_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_organization.office_address IS '직장 주소';


--
-- Name: COLUMN admin_organization.office_department; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_organization.office_department IS '부서명';


--
-- Name: COLUMN admin_organization.office_detail_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_organization.office_detail_address IS '직장 상세주소';


--
-- Name: COLUMN admin_organization.office_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_organization.office_name IS '직장명';


--
-- Name: COLUMN admin_organization.office_position; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin_organization.office_position IS '직위';


--
-- Name: admin_organization_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.admin_organization ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.admin_organization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: answer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.answer (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    author_name character varying(255),
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    question_ref_id bigint NOT NULL,
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT answer_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN answer.author_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.answer.author_name IS '답변자 이름';


--
-- Name: COLUMN answer.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.answer.content_html IS '내용 HTML';


--
-- Name: COLUMN answer.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.answer.content_text IS '내용 텍스트';


--
-- Name: COLUMN answer.question_ref_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.answer.question_ref_id IS '질문 ID';


--
-- Name: COLUMN answer.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.answer.user_id IS '답변자 ID';


--
-- Name: COLUMN answer.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.answer.user_type IS '사용자 타입';


--
-- Name: answer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.answer ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.answer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: archive; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.archive (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    description character varying(255),
    fields jsonb,
    name character varying(255) NOT NULL,
    page_size integer DEFAULT 10 NOT NULL,
    supports_comment boolean DEFAULT false NOT NULL,
    type character varying(255),
    CONSTRAINT archive_type_check CHECK (((type)::text = ANY ((ARRAY['NORMAL'::character varying, 'GALLERY'::character varying])::text[])))
);


--
-- Name: COLUMN archive.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive.description IS '아카이브 설명';


--
-- Name: COLUMN archive.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive.fields IS '추가 필드';


--
-- Name: COLUMN archive.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive.name IS '아카이브 이름';


--
-- Name: COLUMN archive.page_size; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive.page_size IS '페이지 당 글 수';


--
-- Name: COLUMN archive.supports_comment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive.supports_comment IS '댓글 기능 활성화 여부';


--
-- Name: COLUMN archive.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive.type IS '아카이브 타입';


--
-- Name: archive_article; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.archive_article (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    board_id bigint NOT NULL,
    author_name character varying(255),
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    fields jsonb,
    hit integer DEFAULT 0 NOT NULL,
    ip_address character varying(255),
    likes integer DEFAULT 0 NOT NULL,
    status character varying(255),
    title character varying(255),
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT archive_article_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying])::text[]))),
    CONSTRAINT archive_article_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN archive_article.board_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.board_id IS '아카이브 ID';


--
-- Name: COLUMN archive_article.author_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.author_name IS '작성자 이름';


--
-- Name: COLUMN archive_article.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.content_html IS '내용 HTML';


--
-- Name: COLUMN archive_article.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.content_text IS '내용 텍스트';


--
-- Name: COLUMN archive_article.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.fields IS '추가 필드';


--
-- Name: COLUMN archive_article.hit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.hit IS '조회 수';


--
-- Name: COLUMN archive_article.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.ip_address IS 'IP 주소';


--
-- Name: COLUMN archive_article.likes; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.likes IS '추천/좋아요/하트 수';


--
-- Name: COLUMN archive_article.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.status IS '게시 상태';


--
-- Name: COLUMN archive_article.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.title IS '제목';


--
-- Name: COLUMN archive_article.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.user_id IS '사용자 ID';


--
-- Name: COLUMN archive_article.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article.user_type IS '사용자 타입';


--
-- Name: archive_article_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.archive_article_file (
    id bigint NOT NULL,
    article_id bigint NOT NULL,
    file_id bigint NOT NULL,
    sequence integer NOT NULL
);


--
-- Name: COLUMN archive_article_file.article_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article_file.article_id IS '아카이브 글 ID';


--
-- Name: COLUMN archive_article_file.file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article_file.file_id IS '파일 ID';


--
-- Name: COLUMN archive_article_file.sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_article_file.sequence IS '첨부 순서';


--
-- Name: archive_article_file_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.archive_article_file ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.archive_article_file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: archive_article_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.archive_article ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.archive_article_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: archive_comment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.archive_comment (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    article_id bigint NOT NULL,
    author_name character varying(255),
    content text DEFAULT ''::text NOT NULL,
    depth integer DEFAULT 0 NOT NULL,
    hit integer NOT NULL,
    ip_address character varying(255),
    "order" integer DEFAULT 0 NOT NULL,
    parent_id bigint,
    profile_image_file_id bigint,
    sns_type character varying(255),
    sns_user_url character varying(255),
    sns_username character varying(255),
    status character varying(255),
    title character varying(255),
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT archive_comment_sns_type_check CHECK (((sns_type)::text = ANY ((ARRAY['FACEBOOK'::character varying, 'INSTAGRAM'::character varying, 'X'::character varying, 'GOOGLE'::character varying, 'APPLE'::character varying, 'NAVER'::character varying, 'KAKAO'::character varying, 'GITHUB'::character varying])::text[]))),
    CONSTRAINT archive_comment_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'MODERATED'::character varying])::text[]))),
    CONSTRAINT archive_comment_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN archive_comment.article_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.article_id IS '아카이브 글 ID';


--
-- Name: COLUMN archive_comment.author_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.author_name IS '작성자 이름';


--
-- Name: COLUMN archive_comment.content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.content IS '내용';


--
-- Name: COLUMN archive_comment.depth; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.depth IS '댓글 깊이';


--
-- Name: COLUMN archive_comment.hit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.hit IS '조회 수';


--
-- Name: COLUMN archive_comment.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.ip_address IS '작성자 IP 주소';


--
-- Name: COLUMN archive_comment."order"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment."order" IS '댓글 순서';


--
-- Name: COLUMN archive_comment.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.parent_id IS '부모 댓글 ID';


--
-- Name: COLUMN archive_comment.profile_image_file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.profile_image_file_id IS '프로필 이미지 파일 ID';


--
-- Name: COLUMN archive_comment.sns_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.sns_type IS 'SNS 타입';


--
-- Name: COLUMN archive_comment.sns_user_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.sns_user_url IS 'SNS 사용자 URL';


--
-- Name: COLUMN archive_comment.sns_username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.sns_username IS 'SNS 아이디';


--
-- Name: COLUMN archive_comment.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.status IS '댓글 상태';


--
-- Name: COLUMN archive_comment.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.title IS '제목';


--
-- Name: COLUMN archive_comment.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.user_id IS '사용자 ID';


--
-- Name: COLUMN archive_comment.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.archive_comment.user_type IS '사용자 타입';


--
-- Name: archive_comment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.archive_comment ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.archive_comment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: archive_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.archive ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.archive_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: authenticated_guest; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.authenticated_guest (
    connection_info character varying(255),
    duplication_info character varying(255),
    id bigint NOT NULL
);


--
-- Name: COLUMN authenticated_guest.connection_info; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.authenticated_guest.connection_info IS '연계 정보(CI)';


--
-- Name: COLUMN authenticated_guest.duplication_info; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.authenticated_guest.duplication_info IS '중복 정보(DI)';


--
-- Name: authenticated_member; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.authenticated_member (
    connection_info character varying(255),
    duplication_info character varying(255),
    id bigint NOT NULL
);


--
-- Name: COLUMN authenticated_member.connection_info; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.authenticated_member.connection_info IS '연계 정보(CI)';


--
-- Name: COLUMN authenticated_member.duplication_info; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.authenticated_member.duplication_info IS '연계 정보(CI)';


--
-- Name: banner; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.banner (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    background_color character varying(7),
    description character varying(255),
    dismiss_days integer DEFAULT 1 NOT NULL,
    end_date_time timestamp(6) without time zone,
    file_id bigint,
    height integer,
    is_modal boolean DEFAULT false NOT NULL,
    is_using boolean DEFAULT true NOT NULL,
    "left" integer,
    link character varying(255),
    name character varying(255),
    opens_new_window boolean DEFAULT false NOT NULL,
    "order" integer NOT NULL,
    start_date_time timestamp(6) without time zone,
    text_color character varying(7),
    top integer,
    type character varying(255),
    width integer,
    CONSTRAINT banner_type_check CHECK (((type)::text = ANY ((ARRAY['BANNER'::character varying, 'POPUP'::character varying])::text[])))
);


--
-- Name: COLUMN banner.background_color; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.background_color IS '띠배너 배경색 (#RRGGBB)';


--
-- Name: COLUMN banner.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.description IS '배너 설명';


--
-- Name: COLUMN banner.dismiss_days; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.dismiss_days IS '보지 않기로 닫았을 때 다시 보이지 않는 일수 (0: 보지 않기 버튼 없음)';


--
-- Name: COLUMN banner.end_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.end_date_time IS '게시 종료 일시';


--
-- Name: COLUMN banner.file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.file_id IS '파일 ID';


--
-- Name: COLUMN banner.height; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.height IS '세로 길이';


--
-- Name: COLUMN banner.is_modal; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.is_modal IS '팝업 모달 여부 (true: 배경 클릭 불가, false: 모달리스)';


--
-- Name: COLUMN banner.is_using; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.is_using IS '사용 여부';


--
-- Name: COLUMN banner."left"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner."left" IS 'CSS 위치 left';


--
-- Name: COLUMN banner.link; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.link IS '링크';


--
-- Name: COLUMN banner.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.name IS '배너 이름';


--
-- Name: COLUMN banner.opens_new_window; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.opens_new_window IS '새창 열기 여부';


--
-- Name: COLUMN banner."order"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner."order" IS '배너 순서';


--
-- Name: COLUMN banner.start_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.start_date_time IS '게시 시작 일시';


--
-- Name: COLUMN banner.text_color; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.text_color IS '띠배너 글자색 (#RRGGBB)';


--
-- Name: COLUMN banner.top; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.top IS 'CSS 위치 top 값';


--
-- Name: COLUMN banner.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.type IS '배너 타입';


--
-- Name: COLUMN banner.width; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.banner.width IS '가로 길이';


--
-- Name: banner_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.banner ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.banner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: board; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    description character varying(255),
    fields jsonb,
    name character varying(255) NOT NULL,
    type character varying(255),
    CONSTRAINT board_type_check CHECK (((type)::text = ANY ((ARRAY['NORMAL'::character varying, 'GALLERY'::character varying])::text[])))
);


--
-- Name: COLUMN board.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board.description IS '게시판 설명';


--
-- Name: COLUMN board.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board.fields IS '추가 필드';


--
-- Name: COLUMN board.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board.name IS '게시판 이름';


--
-- Name: COLUMN board.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board.type IS '게시판 타입';


--
-- Name: board_article; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_article (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    author_name character varying(255),
    board_id bigint NOT NULL,
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    fields jsonb,
    hit integer DEFAULT 0 NOT NULL,
    ip_address character varying(255) NOT NULL,
    is_hidden boolean DEFAULT false NOT NULL,
    is_notice boolean DEFAULT false NOT NULL,
    is_secret boolean DEFAULT false NOT NULL,
    likes integer DEFAULT 0 NOT NULL,
    notice_end_date_time timestamp(6) without time zone,
    notice_start_date_time timestamp(6) without time zone,
    password character varying(255),
    title character varying(255),
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT board_article_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN board_article.author_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.author_name IS '작성자 이름';


--
-- Name: COLUMN board_article.board_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.board_id IS '게시판 ID';


--
-- Name: COLUMN board_article.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.content_html IS '내용 HTML';


--
-- Name: COLUMN board_article.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.content_text IS '내용 텍스트';


--
-- Name: COLUMN board_article.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.fields IS '추가 필드';


--
-- Name: COLUMN board_article.hit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.hit IS '조회 수';


--
-- Name: COLUMN board_article.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.ip_address IS 'IP 주소';


--
-- Name: COLUMN board_article.is_hidden; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.is_hidden IS '비공개 여부';


--
-- Name: COLUMN board_article.is_notice; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.is_notice IS '공지 여부';


--
-- Name: COLUMN board_article.is_secret; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.is_secret IS '비밀글 여부';


--
-- Name: COLUMN board_article.likes; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.likes IS '추천/좋아요/하트 수';


--
-- Name: COLUMN board_article.notice_end_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.notice_end_date_time IS '공지 종료 일시';


--
-- Name: COLUMN board_article.notice_start_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.notice_start_date_time IS '공지 시작 일시';


--
-- Name: COLUMN board_article.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.password IS '비회원 비밀번호 (해시)';


--
-- Name: COLUMN board_article.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.title IS '제목';


--
-- Name: COLUMN board_article.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.user_id IS '사용자 ID';


--
-- Name: COLUMN board_article.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article.user_type IS '사용자 타입';


--
-- Name: board_article_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_article_file (
    id bigint NOT NULL,
    article_id bigint NOT NULL,
    file_id bigint NOT NULL,
    sequence integer NOT NULL
);


--
-- Name: COLUMN board_article_file.article_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article_file.article_id IS '게시글 ID';


--
-- Name: COLUMN board_article_file.file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article_file.file_id IS '파일 ID';


--
-- Name: COLUMN board_article_file.sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article_file.sequence IS '첨부 순서';


--
-- Name: board_article_file_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.board_article_file ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.board_article_file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: board_article_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.board_article ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.board_article_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: board_article_like; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_article_like (
    id bigint NOT NULL,
    article_id bigint NOT NULL,
    member_id bigint NOT NULL
);


--
-- Name: COLUMN board_article_like.article_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article_like.article_id IS '게시글 ID';


--
-- Name: COLUMN board_article_like.member_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_article_like.member_id IS '회원 ID';


--
-- Name: board_article_like_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.board_article_like ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.board_article_like_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: board_comment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_comment (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    article_id bigint NOT NULL,
    author_name character varying(255),
    content text,
    depth integer DEFAULT 0 NOT NULL,
    hit integer NOT NULL,
    ip_address character varying(255),
    "order" integer DEFAULT 0 NOT NULL,
    parent_id bigint,
    password character varying(255),
    profile_image_file_id bigint,
    sns_type character varying(255),
    sns_user_url character varying(255),
    sns_username character varying(255),
    status character varying(255),
    title character varying(255),
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT board_comment_sns_type_check CHECK (((sns_type)::text = ANY ((ARRAY['FACEBOOK'::character varying, 'INSTAGRAM'::character varying, 'X'::character varying, 'GOOGLE'::character varying, 'APPLE'::character varying, 'NAVER'::character varying, 'KAKAO'::character varying, 'GITHUB'::character varying])::text[]))),
    CONSTRAINT board_comment_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'MODERATED'::character varying])::text[]))),
    CONSTRAINT board_comment_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN board_comment.article_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.article_id IS '게시글 ID';


--
-- Name: COLUMN board_comment.author_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.author_name IS '작성자 이름';


--
-- Name: COLUMN board_comment.content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.content IS '내용';


--
-- Name: COLUMN board_comment.depth; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.depth IS '댓글 깊이';


--
-- Name: COLUMN board_comment.hit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.hit IS '조회 수';


--
-- Name: COLUMN board_comment.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.ip_address IS '작성자 IP 주소';


--
-- Name: COLUMN board_comment."order"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment."order" IS '댓글 순서';


--
-- Name: COLUMN board_comment.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.parent_id IS '부모 댓글 ID';


--
-- Name: COLUMN board_comment.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.password IS '비회원 비밀번호 (해시)';


--
-- Name: COLUMN board_comment.profile_image_file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.profile_image_file_id IS '프로필 이미지 파일 ID';


--
-- Name: COLUMN board_comment.sns_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.sns_type IS 'SNS 타입';


--
-- Name: COLUMN board_comment.sns_user_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.sns_user_url IS 'SNS 사용자 URL';


--
-- Name: COLUMN board_comment.sns_username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.sns_username IS 'SNS 아이디';


--
-- Name: COLUMN board_comment.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.status IS '댓글 상태';


--
-- Name: COLUMN board_comment.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.title IS '제목';


--
-- Name: COLUMN board_comment.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.user_id IS '사용자 ID';


--
-- Name: COLUMN board_comment.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_comment.user_type IS '사용자 타입';


--
-- Name: board_comment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.board_comment ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.board_comment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: board_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.board_config (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    board_id bigint NOT NULL,
    is_guest_accessible boolean DEFAULT false NOT NULL,
    is_guest_readable boolean DEFAULT false NOT NULL,
    is_guest_writable boolean DEFAULT false NOT NULL,
    is_member_accessible boolean DEFAULT false NOT NULL,
    is_member_readable boolean DEFAULT false NOT NULL,
    is_member_writable boolean DEFAULT false NOT NULL,
    maximum_file_count integer DEFAULT 5 NOT NULL,
    page_size integer DEFAULT 10 NOT NULL,
    supports_attachment boolean DEFAULT false NOT NULL,
    supports_comment boolean DEFAULT false NOT NULL,
    supports_like boolean DEFAULT false NOT NULL,
    supports_notice boolean DEFAULT false NOT NULL,
    supports_secret boolean DEFAULT false NOT NULL
);


--
-- Name: COLUMN board_config.board_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.board_id IS '게시판 ID';


--
-- Name: COLUMN board_config.is_guest_accessible; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.is_guest_accessible IS '비회원 목록 조회 권한';


--
-- Name: COLUMN board_config.is_guest_readable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.is_guest_readable IS '비회원 글 읽기 권한';


--
-- Name: COLUMN board_config.is_guest_writable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.is_guest_writable IS '비회원 글 쓰기 권한';


--
-- Name: COLUMN board_config.is_member_accessible; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.is_member_accessible IS '회원 목록 조회 권한';


--
-- Name: COLUMN board_config.is_member_readable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.is_member_readable IS '회원 글 읽기 권한';


--
-- Name: COLUMN board_config.is_member_writable; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.is_member_writable IS '회원 글 쓰기 권한';


--
-- Name: COLUMN board_config.maximum_file_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.maximum_file_count IS '최대 첨부 파일 수';


--
-- Name: COLUMN board_config.page_size; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.page_size IS '페이지 당 글 수';


--
-- Name: COLUMN board_config.supports_attachment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.supports_attachment IS '첨부 파일 기능 활성화 여부';


--
-- Name: COLUMN board_config.supports_comment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.supports_comment IS '댓글 기능 활성화 여부';


--
-- Name: COLUMN board_config.supports_like; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.supports_like IS '추천/좋아요/하트 기능 활성화 여부';


--
-- Name: COLUMN board_config.supports_notice; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.supports_notice IS '공지 기능 활성화 여부';


--
-- Name: COLUMN board_config.supports_secret; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.board_config.supports_secret IS '비밀글 기능 활성화 여부';


--
-- Name: board_config_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.board_config ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.board_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: board_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.board ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.board_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: branch; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.branch (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    address character varying(255) NOT NULL,
    business_hours character varying(255),
    description character varying(255),
    detail_address character varying(255),
    is_using boolean DEFAULT true NOT NULL,
    latitude numeric(10,7) NOT NULL,
    longitude numeric(10,7) NOT NULL,
    name character varying(255) NOT NULL,
    phone_number character varying(255),
    sequence integer DEFAULT 0 NOT NULL
);


--
-- Name: COLUMN branch.address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.address IS '주소';


--
-- Name: COLUMN branch.business_hours; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.business_hours IS '운영 시간';


--
-- Name: COLUMN branch.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.description IS '안내';


--
-- Name: COLUMN branch.detail_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.detail_address IS '상세 주소';


--
-- Name: COLUMN branch.is_using; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.is_using IS '노출 여부';


--
-- Name: COLUMN branch.latitude; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.latitude IS '위도';


--
-- Name: COLUMN branch.longitude; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.longitude IS '경도';


--
-- Name: COLUMN branch.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.name IS '지점명';


--
-- Name: COLUMN branch.phone_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.phone_number IS '연락처';


--
-- Name: COLUMN branch.sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.branch.sequence IS '표시 순서';


--
-- Name: branch_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.branch ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.branch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: capability; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.capability (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    description character varying(255),
    is_default boolean DEFAULT false NOT NULL,
    name character varying(255),
    priority integer NOT NULL,
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT capability_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN capability.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.capability.description IS '권한 설명';


--
-- Name: COLUMN capability.is_default; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.capability.is_default IS '기본 여부';


--
-- Name: COLUMN capability.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.capability.name IS '권한 이름';


--
-- Name: COLUMN capability.priority; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.capability.priority IS '우선순위';


--
-- Name: COLUMN capability.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.capability.user_id IS '사용자 ID';


--
-- Name: COLUMN capability.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.capability.user_type IS '사용자 타입';


--
-- Name: capability_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.capability ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.capability_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.code (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    depth integer DEFAULT 0 NOT NULL,
    description character varying(255),
    english_name character varying(255),
    is_using boolean DEFAULT true NOT NULL,
    name character varying(255),
    "order" integer DEFAULT 0 NOT NULL,
    parent_id bigint
);


--
-- Name: COLUMN code.depth; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.code.depth IS '코드 깊이';


--
-- Name: COLUMN code.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.code.description IS '코드 설명';


--
-- Name: COLUMN code.english_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.code.english_name IS '코드 영어 이름';


--
-- Name: COLUMN code.is_using; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.code.is_using IS '사용 여부';


--
-- Name: COLUMN code.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.code.name IS '코드 이름';


--
-- Name: COLUMN code."order"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.code."order" IS '코드 순서';


--
-- Name: COLUMN code.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.code.parent_id IS '부모 코드 ID';


--
-- Name: code_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.code ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.config (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    description character varying(255) DEFAULT ''::character varying NOT NULL,
    is_hidden boolean DEFAULT false NOT NULL,
    key character varying(255) NOT NULL,
    type character varying(255),
    value character varying(255) DEFAULT ''::character varying NOT NULL,
    CONSTRAINT config_type_check CHECK (((type)::text = ANY ((ARRAY['TEXT'::character varying, 'RADIO'::character varying, 'CHECKBOX'::character varying, 'TEXTAREA'::character varying])::text[])))
);


--
-- Name: COLUMN config.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.config.description IS '옵션 설명';


--
-- Name: COLUMN config.is_hidden; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.config.is_hidden IS '숨김 여부';


--
-- Name: COLUMN config.key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.config.key IS '옵션 키';


--
-- Name: COLUMN config.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.config.type IS '옵션 타입';


--
-- Name: COLUMN config.value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.config.value IS '옵션 값';


--
-- Name: config_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.config ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.content (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    last_worker character varying(255),
    memo character varying(255) DEFAULT ''::character varying NOT NULL,
    status character varying(255),
    title character varying(255),
    version integer DEFAULT 1 NOT NULL,
    CONSTRAINT content_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying])::text[])))
);


--
-- Name: COLUMN content.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.content.content_html IS '내용 HTML';


--
-- Name: COLUMN content.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.content.content_text IS '내용 텍스트';


--
-- Name: COLUMN content.last_worker; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.content.last_worker IS '마지막 작업자';


--
-- Name: COLUMN content.memo; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.content.memo IS '메모';


--
-- Name: COLUMN content.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.content.status IS '컨텐츠 상태';


--
-- Name: COLUMN content.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.content.title IS '제목';


--
-- Name: COLUMN content.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.content.version IS '컨텐츠 버전';


--
-- Name: content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.content ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: discount; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.discount (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    description character varying(255),
    is_using boolean DEFAULT true NOT NULL,
    name character varying(255),
    product_id bigint,
    rate numeric(5,2)
);


--
-- Name: COLUMN discount.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.discount.description IS '할인 설명';


--
-- Name: COLUMN discount.is_using; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.discount.is_using IS '사용 여부';


--
-- Name: COLUMN discount.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.discount.name IS '할인명';


--
-- Name: COLUMN discount.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.discount.product_id IS '상품 ID';


--
-- Name: COLUMN discount.rate; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.discount.rate IS '할인율 (%)';


--
-- Name: discount_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.discount ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.discount_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.file (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    alternative_text character varying(255),
    download_count integer DEFAULT 0 NOT NULL,
    extension character varying(255),
    media_type character varying(255),
    mime_type character varying(255),
    original_name character varying(255),
    path character varying(255),
    size bigint,
    uuid character varying(255),
    CONSTRAINT file_media_type_check CHECK (((media_type)::text = ANY ((ARRAY['DOCUMENT'::character varying, 'IMAGE'::character varying, 'VIDEO'::character varying, 'ETC'::character varying])::text[])))
);


--
-- Name: COLUMN file.alternative_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.alternative_text IS '대체 텍스트';


--
-- Name: COLUMN file.download_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.download_count IS '다운로드 수';


--
-- Name: COLUMN file.extension; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.extension IS '확장자';


--
-- Name: COLUMN file.media_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.media_type IS '미디어 종류';


--
-- Name: COLUMN file.mime_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.mime_type IS 'MIME 타입';


--
-- Name: COLUMN file.original_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.original_name IS '파일 원본 이름';


--
-- Name: COLUMN file.path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.path IS '파일 경로';


--
-- Name: COLUMN file.size; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.size IS '파일 사이즈';


--
-- Name: COLUMN file.uuid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file.uuid IS '파일 UUID';


--
-- Name: file_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.file ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."group" (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    depth integer DEFAULT 0 NOT NULL,
    description character varying(255),
    name character varying(255),
    "order" integer DEFAULT 0 NOT NULL,
    parent_id bigint
);


--
-- Name: COLUMN "group".depth; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."group".depth IS '그룹 깊이';


--
-- Name: COLUMN "group".description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."group".description IS '그룹 설명';


--
-- Name: COLUMN "group".name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."group".name IS '그룹 이름';


--
-- Name: COLUMN "group"."order"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."group"."order" IS '그룹 순서';


--
-- Name: COLUMN "group".parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."group".parent_id IS '부모 그룹 ID';


--
-- Name: group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public."group" ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: guest; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.guest (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    birth_date date,
    email character varying(255),
    gender character varying(255),
    ip_address character varying(255),
    mobile_phone_name character varying(255),
    name character varying(255),
    telephone_name character varying(255)
);


--
-- Name: COLUMN guest.birth_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.guest.birth_date IS '생년월일';


--
-- Name: COLUMN guest.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.guest.email IS '이메일';


--
-- Name: COLUMN guest.gender; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.guest.gender IS '성별';


--
-- Name: COLUMN guest.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.guest.ip_address IS 'IP 주소';


--
-- Name: COLUMN guest.mobile_phone_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.guest.mobile_phone_name IS '휴대폰 번호';


--
-- Name: COLUMN guest.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.guest.name IS '이름';


--
-- Name: COLUMN guest.telephone_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.guest.telephone_name IS '전화번호';


--
-- Name: guest_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.guest ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.guest_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: image_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.image_file (
    original_height integer NOT NULL,
    original_width integer NOT NULL,
    thumbnail_height integer NOT NULL,
    thumbnail_path character varying(255),
    thumbnail_width integer NOT NULL,
    id bigint NOT NULL
);


--
-- Name: COLUMN image_file.original_height; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.image_file.original_height IS '원본 세로 길이';


--
-- Name: COLUMN image_file.original_width; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.image_file.original_width IS '원본 가로 길이';


--
-- Name: COLUMN image_file.thumbnail_height; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.image_file.thumbnail_height IS '썸네일 세로 길이';


--
-- Name: COLUMN image_file.thumbnail_path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.image_file.thumbnail_path IS '썸네일 경로';


--
-- Name: COLUMN image_file.thumbnail_width; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.image_file.thumbnail_width IS '썸네일 가로 길이';


--
-- Name: map_setting; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.map_setting (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    branches_enabled boolean DEFAULT false NOT NULL,
    directions_enabled boolean DEFAULT false NOT NULL,
    kakao_app_key character varying(255),
    naver_client_id character varying(255),
    office_address character varying(255),
    office_business_hours character varying(255),
    office_description text,
    office_detail_address character varying(255),
    office_latitude numeric(10,7),
    office_longitude numeric(10,7),
    office_name character varying(255),
    office_phone_number character varying(255),
    provider character varying(20) NOT NULL,
    CONSTRAINT map_setting_provider_check CHECK (((provider)::text = ANY ((ARRAY['KAKAO'::character varying, 'NAVER'::character varying])::text[])))
);


--
-- Name: COLUMN map_setting.branches_enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.branches_enabled IS '지점 안내 사용 여부';


--
-- Name: COLUMN map_setting.directions_enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.directions_enabled IS '오시는 길 사용 여부';


--
-- Name: COLUMN map_setting.kakao_app_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.kakao_app_key IS '카카오맵 JavaScript 키';


--
-- Name: COLUMN map_setting.naver_client_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.naver_client_id IS '네이버 지도 Client ID (ncpKeyId)';


--
-- Name: COLUMN map_setting.office_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.office_address IS '본점 주소';


--
-- Name: COLUMN map_setting.office_business_hours; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.office_business_hours IS '본점 운영 시간';


--
-- Name: COLUMN map_setting.office_description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.office_description IS '오시는 길 안내 (교통편 등)';


--
-- Name: COLUMN map_setting.office_detail_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.office_detail_address IS '본점 상세 주소';


--
-- Name: COLUMN map_setting.office_latitude; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.office_latitude IS '본점 위도';


--
-- Name: COLUMN map_setting.office_longitude; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.office_longitude IS '본점 경도';


--
-- Name: COLUMN map_setting.office_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.office_name IS '본점 이름';


--
-- Name: COLUMN map_setting.office_phone_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.office_phone_number IS '본점 연락처';


--
-- Name: COLUMN map_setting.provider; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.map_setting.provider IS '지도 제공자';


--
-- Name: map_setting_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.map_setting ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.map_setting_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: member; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.member (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    birth_date date,
    email character varying(255),
    first_name character varying(255),
    gender character varying(255),
    group_id bigint,
    is_lunar_birth_date boolean DEFAULT false NOT NULL,
    last_login_date_time timestamp(6) without time zone,
    last_name character varying(255),
    login_try_count integer DEFAULT 0 NOT NULL,
    mobile_phone_number character varying(255),
    name character varying(255),
    password character varying(255) NOT NULL,
    role_id bigint,
    telephone_number character varying(255),
    term_confirmation_date date,
    term_expiration_date date,
    username character varying(255) NOT NULL
);


--
-- Name: COLUMN member.birth_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.birth_date IS '생년월일';


--
-- Name: COLUMN member.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.email IS '이메일';


--
-- Name: COLUMN member.first_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.first_name IS '이름만';


--
-- Name: COLUMN member.gender; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.gender IS '성별';


--
-- Name: COLUMN member.group_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.group_id IS '그룹';


--
-- Name: COLUMN member.is_lunar_birth_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.is_lunar_birth_date IS '음력 여부';


--
-- Name: COLUMN member.last_login_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.last_login_date_time IS '마지막 로그인 일시';


--
-- Name: COLUMN member.last_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.last_name IS '성';


--
-- Name: COLUMN member.login_try_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.login_try_count IS '로그인 시도 횟수';


--
-- Name: COLUMN member.mobile_phone_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.mobile_phone_number IS '휴대폰 번호';


--
-- Name: COLUMN member.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.name IS '이름';


--
-- Name: COLUMN member.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.password IS '비밀번호';


--
-- Name: COLUMN member.role_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.role_id IS '권한';


--
-- Name: COLUMN member.telephone_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.telephone_number IS '전화번호';


--
-- Name: COLUMN member.term_confirmation_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.term_confirmation_date IS '약관 동의 확인 일시';


--
-- Name: COLUMN member.term_expiration_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.term_expiration_date IS '약관 동의 만료 일시';


--
-- Name: COLUMN member.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member.username IS '회원 ID';


--
-- Name: member_address; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.member_address (
    id bigint NOT NULL,
    address character varying(255),
    detail_address character varying(255),
    member_id bigint NOT NULL,
    postal_code character varying(255)
);


--
-- Name: COLUMN member_address.address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_address.address IS '주소';


--
-- Name: COLUMN member_address.detail_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_address.detail_address IS '상세주소';


--
-- Name: COLUMN member_address.member_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_address.member_id IS '회원 ID';


--
-- Name: COLUMN member_address.postal_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_address.postal_code IS '우편번호';


--
-- Name: member_address_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.member_address ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.member_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: member_agreement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.member_agreement (
    id bigint NOT NULL,
    is_mail_reception_agreed boolean DEFAULT false NOT NULL,
    is_marketing_usage_agreed boolean DEFAULT false NOT NULL,
    is_sms_reception_agreed boolean DEFAULT false NOT NULL,
    member_id bigint NOT NULL
);


--
-- Name: COLUMN member_agreement.is_mail_reception_agreed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_agreement.is_mail_reception_agreed IS '메일 수신 동의 여부';


--
-- Name: COLUMN member_agreement.is_marketing_usage_agreed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_agreement.is_marketing_usage_agreed IS '마케팅 활용 동의 여부';


--
-- Name: COLUMN member_agreement.is_sms_reception_agreed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_agreement.is_sms_reception_agreed IS 'SMS 수신 동의 여부';


--
-- Name: COLUMN member_agreement.member_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_agreement.member_id IS '회원 ID';


--
-- Name: member_agreement_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.member_agreement ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.member_agreement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: member_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.member ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.member_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: member_organization; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.member_organization (
    id bigint NOT NULL,
    member_id bigint NOT NULL,
    office_address character varying(255),
    office_department character varying(255),
    office_detail_address character varying(255),
    office_name character varying(255),
    office_position character varying(255)
);


--
-- Name: COLUMN member_organization.member_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_organization.member_id IS '회원 ID';


--
-- Name: COLUMN member_organization.office_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_organization.office_address IS '직장 주소';


--
-- Name: COLUMN member_organization.office_department; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_organization.office_department IS '부서명';


--
-- Name: COLUMN member_organization.office_detail_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_organization.office_detail_address IS '직장 상세주소';


--
-- Name: COLUMN member_organization.office_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_organization.office_name IS '직장명';


--
-- Name: COLUMN member_organization.office_position; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.member_organization.office_position IS '직위';


--
-- Name: member_organization_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.member_organization ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.member_organization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: menu; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.menu (
    dtype character varying(31) NOT NULL,
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    depth integer DEFAULT 0 NOT NULL,
    feature character varying(50),
    is_using boolean DEFAULT true NOT NULL,
    link character varying(255),
    name character varying(255),
    parent_id bigint,
    sequence integer DEFAULT 0 NOT NULL,
    target_id bigint,
    type character varying(255),
    menu_footer character varying(255),
    menu_header character varying(255),
    menu_template character varying(255),
    CONSTRAINT menu_dtype_check CHECK (((dtype)::text = ANY ((ARRAY['Menu'::character varying, 'MenuTemplate'::character varying])::text[]))),
    CONSTRAINT menu_feature_check CHECK (((feature)::text = ANY ((ARRAY['CONTENT'::character varying, 'BOARD'::character varying, 'ARCHIVE'::character varying, 'QNA'::character varying, 'MAP'::character varying, 'PRODUCT'::character varying, 'PROGRAM'::character varying, 'REPORT'::character varying, 'SATISFACTION'::character varying])::text[]))),
    CONSTRAINT menu_type_check CHECK (((type)::text = ANY ((ARRAY['CONTENT'::character varying, 'BOARD'::character varying, 'LINK'::character varying, 'FEATURE'::character varying])::text[])))
);


--
-- Name: COLUMN menu.depth; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.depth IS '깊이';


--
-- Name: COLUMN menu.feature; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.feature IS '기능 메뉴가 가리키는 기능 (FEATURE 메뉴만)';


--
-- Name: COLUMN menu.is_using; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.is_using IS '사용 여부';


--
-- Name: COLUMN menu.link; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.link IS '링크';


--
-- Name: COLUMN menu.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.name IS '메뉴 이름';


--
-- Name: COLUMN menu.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.parent_id IS '부모 메뉴 ID';


--
-- Name: COLUMN menu.sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.sequence IS '순서';


--
-- Name: COLUMN menu.target_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.target_id IS '연결 대상 ID (CONTENT: 컨텐츠 ID, BOARD: 게시판 ID)';


--
-- Name: COLUMN menu.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.menu.type IS '메뉴 종류';


--
-- Name: menu_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.menu ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.menu_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."order" (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    address character varying(255),
    can_deliver boolean DEFAULT false NOT NULL,
    detail_address character varying(255),
    discount_id bigint,
    fields jsonb,
    order_number character varying(255) NOT NULL,
    order_sequence character varying(255),
    phone_number character varying(255),
    postal_code character varying(255),
    product_id bigint,
    product_name character varying(255),
    product_option_id bigint,
    quantity integer,
    receiver_name character varying(255),
    status character varying(255),
    total_price numeric(15,2),
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT order_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'PAID'::character varying, 'SHIPPING'::character varying, 'DELIVERED'::character varying, 'CANCELED'::character varying])::text[]))),
    CONSTRAINT order_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN "order".address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".address IS '주소';


--
-- Name: COLUMN "order".can_deliver; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".can_deliver IS '배송 가능 여부';


--
-- Name: COLUMN "order".detail_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".detail_address IS '상세 주소';


--
-- Name: COLUMN "order".discount_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".discount_id IS '할인 ID';


--
-- Name: COLUMN "order".fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".fields IS '추가 필드';


--
-- Name: COLUMN "order".order_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".order_number IS '주문 번호';


--
-- Name: COLUMN "order".order_sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".order_sequence IS '주문 순번';


--
-- Name: COLUMN "order".phone_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".phone_number IS '연락처';


--
-- Name: COLUMN "order".postal_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".postal_code IS '우편번호';


--
-- Name: COLUMN "order".product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".product_id IS '상품 ID';


--
-- Name: COLUMN "order".product_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".product_name IS '주문 당시 상품명';


--
-- Name: COLUMN "order".product_option_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".product_option_id IS '상품 옵션 ID';


--
-- Name: COLUMN "order".quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".quantity IS '수량';


--
-- Name: COLUMN "order".receiver_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".receiver_name IS '수령인';


--
-- Name: COLUMN "order".status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".status IS '주문 상태';


--
-- Name: COLUMN "order".total_price; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".total_price IS '총 금액';


--
-- Name: COLUMN "order".user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".user_id IS '사용자 ID';


--
-- Name: COLUMN "order".user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public."order".user_type IS '사용자 타입';


--
-- Name: order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public."order" ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: payment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    amount numeric(15,2) NOT NULL,
    approved_at timestamp(6) without time zone,
    canceled_at timestamp(6) without time zone,
    member_id bigint NOT NULL,
    method character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    target_id bigint NOT NULL,
    target_type character varying(255) NOT NULL,
    transaction_id character varying(255) NOT NULL,
    CONSTRAINT payment_method_check CHECK (((method)::text = ANY ((ARRAY['CARD'::character varying, 'BANK_TRANSFER'::character varying, 'VIRTUAL_ACCOUNT'::character varying, 'MOBILE'::character varying])::text[]))),
    CONSTRAINT payment_status_check CHECK (((status)::text = ANY ((ARRAY['APPROVED'::character varying, 'CANCELED'::character varying])::text[]))),
    CONSTRAINT payment_target_type_check CHECK (((target_type)::text = ANY ((ARRAY['ORDER'::character varying, 'RESERVATION'::character varying])::text[])))
);


--
-- Name: COLUMN payment.amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.amount IS '결제 금액';


--
-- Name: COLUMN payment.approved_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.approved_at IS '승인 일시';


--
-- Name: COLUMN payment.canceled_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.canceled_at IS '취소 일시';


--
-- Name: COLUMN payment.member_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.member_id IS '결제 회원 ID';


--
-- Name: COLUMN payment.method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.method IS '결제 수단';


--
-- Name: COLUMN payment.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.status IS '결제 상태';


--
-- Name: COLUMN payment.target_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.target_id IS '결제 대상 ID (주문/예약)';


--
-- Name: COLUMN payment.target_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.target_type IS '결제 대상 종류';


--
-- Name: COLUMN payment.transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment.transaction_id IS 'PG 거래 ID (목업)';


--
-- Name: payment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.payment ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: policy; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.policy (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    content_html text DEFAULT ''::text NOT NULL,
    last_worker character varying(255),
    type character varying(30) NOT NULL,
    version integer DEFAULT 1 NOT NULL,
    CONSTRAINT policy_type_check CHECK (((type)::text = ANY ((ARRAY['PRIVACY_CONSENT'::character varying, 'PRIVACY_POLICY'::character varying])::text[])))
);


--
-- Name: COLUMN policy.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.policy.content_html IS '내용 HTML';


--
-- Name: COLUMN policy.last_worker; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.policy.last_worker IS '마지막 수정자';


--
-- Name: COLUMN policy.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.policy.type IS '문서 종류';


--
-- Name: COLUMN policy.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.policy.version IS '버전';


--
-- Name: policy_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.policy ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.policy_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    available_quantity integer,
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    description character varying(255),
    fields jsonb,
    image_file_id bigint,
    is_enabled boolean DEFAULT true NOT NULL,
    name character varying(255),
    price numeric(15,2) NOT NULL
);


--
-- Name: COLUMN product.available_quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.available_quantity IS '재고 수량 (null: 무제한)';


--
-- Name: COLUMN product.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.content_html IS '내용 HTML';


--
-- Name: COLUMN product.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.content_text IS '내용 텍스트';


--
-- Name: COLUMN product.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.description IS '상품 설명';


--
-- Name: COLUMN product.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.fields IS '추가 필드';


--
-- Name: COLUMN product.image_file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.image_file_id IS '대표 이미지 파일 ID';


--
-- Name: COLUMN product.is_enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.is_enabled IS '판매 여부';


--
-- Name: COLUMN product.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.name IS '상품명';


--
-- Name: COLUMN product.price; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product.price IS '가격';


--
-- Name: product_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.product ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.product_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: product_option; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_option (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    additional_price numeric(15,2) DEFAULT 0 NOT NULL,
    description character varying(255),
    fields jsonb,
    name character varying(255),
    product_id bigint NOT NULL
);


--
-- Name: COLUMN product_option.additional_price; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product_option.additional_price IS '추가 금액';


--
-- Name: COLUMN product_option.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product_option.description IS '옵션 설명';


--
-- Name: COLUMN product_option.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product_option.fields IS '추가 필드';


--
-- Name: COLUMN product_option.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product_option.name IS '옵션명';


--
-- Name: COLUMN product_option.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.product_option.product_id IS '상품 ID';


--
-- Name: product_option_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.product_option ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.product_option_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: program; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.program (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    application_end_date_time timestamp(6) without time zone,
    application_start_date_time timestamp(6) without time zone,
    available_person integer,
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    description character varying(255),
    end_date_time timestamp(6) without time zone,
    fields jsonb,
    image_file_id bigint,
    is_enabled boolean DEFAULT true NOT NULL,
    maximum_person integer,
    name character varying(255),
    price numeric(15,2) NOT NULL,
    start_date_time timestamp(6) without time zone
);


--
-- Name: COLUMN program.application_end_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.application_end_date_time IS '신청 종료 일시';


--
-- Name: COLUMN program.application_start_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.application_start_date_time IS '신청 시작 일시';


--
-- Name: COLUMN program.available_person; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.available_person IS '잔여 인원';


--
-- Name: COLUMN program.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.content_html IS '내용 HTML';


--
-- Name: COLUMN program.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.content_text IS '내용 텍스트';


--
-- Name: COLUMN program.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.description IS '프로그램 설명';


--
-- Name: COLUMN program.end_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.end_date_time IS '종료 일시';


--
-- Name: COLUMN program.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.fields IS '추가 필드';


--
-- Name: COLUMN program.image_file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.image_file_id IS '대표 이미지 파일 ID';


--
-- Name: COLUMN program.is_enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.is_enabled IS '사용 여부';


--
-- Name: COLUMN program.maximum_person; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.maximum_person IS '최대 인원';


--
-- Name: COLUMN program.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.name IS '프로그램명';


--
-- Name: COLUMN program.price; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.price IS '참가비 (1인)';


--
-- Name: COLUMN program.start_date_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.program.start_date_time IS '시작 일시';


--
-- Name: program_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.program ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.program_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: question; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    author_name character varying(255),
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    email character varying(255),
    fields jsonb,
    hit integer DEFAULT 0 NOT NULL,
    ip_address character varying(255),
    is_secret boolean DEFAULT false NOT NULL,
    likes integer DEFAULT 0 NOT NULL,
    password character varying(255),
    title character varying(255) DEFAULT '제목 없음'::character varying,
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT question_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN question.author_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.author_name IS '작성자 이름';


--
-- Name: COLUMN question.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.content_html IS '내용 HTML';


--
-- Name: COLUMN question.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.content_text IS '내용 텍스트';


--
-- Name: COLUMN question.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.email IS '이메일';


--
-- Name: COLUMN question.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.fields IS '추가 필드';


--
-- Name: COLUMN question.hit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.hit IS '조회 수';


--
-- Name: COLUMN question.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.ip_address IS 'IP 주소';


--
-- Name: COLUMN question.is_secret; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.is_secret IS '비밀글 여부';


--
-- Name: COLUMN question.likes; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.likes IS '추천/좋아요/하트 수';


--
-- Name: COLUMN question.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.password IS '비회원 비밀번호 (해시)';


--
-- Name: COLUMN question.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.title IS '제목';


--
-- Name: COLUMN question.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.user_id IS '사용자 ID';


--
-- Name: COLUMN question.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question.user_type IS '사용자 타입';


--
-- Name: question_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.question ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.question_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: report; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.report (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    email character varying(255),
    fields character varying(255),
    password character varying(255),
    phone_number character varying(255),
    reporter_name character varying(255),
    status character varying(255),
    title character varying(255) DEFAULT '제목 없음'::character varying NOT NULL,
    user_id bigint,
    CONSTRAINT report_status_check CHECK (((status)::text = ANY ((ARRAY['RECEIVED'::character varying, 'IN_PROGRESS'::character varying, 'ANSWERED'::character varying, 'CLOSED'::character varying])::text[])))
);


--
-- Name: COLUMN report.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.content_html IS '내용 HTML';


--
-- Name: COLUMN report.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.content_text IS '내용 텍스트';


--
-- Name: COLUMN report.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.email IS '사용자 이메일';


--
-- Name: COLUMN report.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.fields IS '추가 필드';


--
-- Name: COLUMN report.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.password IS '비회원 조회 비밀번호 (해시)';


--
-- Name: COLUMN report.phone_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.phone_number IS '사용자 전화번호';


--
-- Name: COLUMN report.reporter_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.reporter_name IS '신고자 이름';


--
-- Name: COLUMN report.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.status IS '처리 상태';


--
-- Name: COLUMN report.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.title IS '제목';


--
-- Name: COLUMN report.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report.user_id IS '사용자 고유식별번호';


--
-- Name: report_answer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.report_answer (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    author_name character varying(255),
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    depth integer DEFAULT 0 NOT NULL,
    "order" integer DEFAULT 0 NOT NULL,
    report_ref_id bigint NOT NULL,
    status character varying(255),
    title character varying(255) DEFAULT '제목 없음'::character varying NOT NULL,
    CONSTRAINT report_answer_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'MODERATED'::character varying])::text[])))
);


--
-- Name: COLUMN report_answer.author_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_answer.author_name IS '답변자 이름';


--
-- Name: COLUMN report_answer.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_answer.content_html IS '내용 HTML';


--
-- Name: COLUMN report_answer.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_answer.content_text IS '내용 텍스트';


--
-- Name: COLUMN report_answer.depth; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_answer.depth IS '깊이';


--
-- Name: COLUMN report_answer."order"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_answer."order" IS '순서';


--
-- Name: COLUMN report_answer.report_ref_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_answer.report_ref_id IS '신고 ID';


--
-- Name: COLUMN report_answer.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_answer.status IS '댓글 상태';


--
-- Name: COLUMN report_answer.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_answer.title IS '제목';


--
-- Name: report_answer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.report_answer ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.report_answer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: report_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.report ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.report_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: reservation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reservation (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    applicant_name character varying(255),
    discount_id bigint,
    fields jsonb,
    phone_number character varying(255),
    product_id bigint,
    program_name character varying(255),
    quantity integer,
    reservation_number character varying(255) NOT NULL,
    reservation_sequence character varying(255),
    status character varying(255),
    total_price numeric(15,2),
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT reservation_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'CONFIRMED'::character varying, 'CANCELED'::character varying])::text[]))),
    CONSTRAINT reservation_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN reservation.applicant_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.applicant_name IS '신청자 이름';


--
-- Name: COLUMN reservation.discount_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.discount_id IS '할인 ID';


--
-- Name: COLUMN reservation.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.fields IS '추가 필드';


--
-- Name: COLUMN reservation.phone_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.phone_number IS '연락처';


--
-- Name: COLUMN reservation.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.product_id IS '프로그램 ID';


--
-- Name: COLUMN reservation.program_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.program_name IS '예약 당시 프로그램명';


--
-- Name: COLUMN reservation.quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.quantity IS '인원';


--
-- Name: COLUMN reservation.reservation_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.reservation_number IS '예약 번호';


--
-- Name: COLUMN reservation.reservation_sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.reservation_sequence IS '예약 순번';


--
-- Name: COLUMN reservation.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.status IS '예약 상태';


--
-- Name: COLUMN reservation.total_price; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.total_price IS '총 금액';


--
-- Name: COLUMN reservation.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.user_id IS '사용자 ID';


--
-- Name: COLUMN reservation.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reservation.user_type IS '사용자 타입';


--
-- Name: reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.reservation ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.reservation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: review; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.review (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    author_name character varying(255),
    content_html text DEFAULT ''::text NOT NULL,
    content_text text DEFAULT ''::text NOT NULL,
    fields jsonb,
    hit integer DEFAULT 0 NOT NULL,
    ip_address character varying(255),
    is_secret boolean DEFAULT false NOT NULL,
    likes integer DEFAULT 0 NOT NULL,
    password character varying(255),
    product_id bigint,
    program_id bigint,
    rating integer DEFAULT 5 NOT NULL,
    target_type character varying(20) DEFAULT 'PRODUCT'::character varying NOT NULL,
    title character varying(255) DEFAULT '제목 없음'::character varying,
    user_id bigint,
    user_type character varying(255),
    CONSTRAINT review_target_type_check CHECK (((target_type)::text = ANY ((ARRAY['PRODUCT'::character varying, 'PROGRAM'::character varying])::text[]))),
    CONSTRAINT review_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'MEMBER'::character varying, 'GUEST'::character varying])::text[])))
);


--
-- Name: COLUMN review.author_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.author_name IS '작성자 이름';


--
-- Name: COLUMN review.content_html; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.content_html IS '내용 HTML';


--
-- Name: COLUMN review.content_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.content_text IS '내용 텍스트';


--
-- Name: COLUMN review.fields; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.fields IS '추가 필드';


--
-- Name: COLUMN review.hit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.hit IS '조회 수';


--
-- Name: COLUMN review.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.ip_address IS 'IP 주소';


--
-- Name: COLUMN review.is_secret; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.is_secret IS '비밀글 여부';


--
-- Name: COLUMN review.likes; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.likes IS '추천/좋아요/하트 수';


--
-- Name: COLUMN review.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.password IS '비밀번호';


--
-- Name: COLUMN review.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.product_id IS '상품 ID';


--
-- Name: COLUMN review.program_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.program_id IS '프로그램 ID';


--
-- Name: COLUMN review.rating; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.rating IS '평점 (1~5)';


--
-- Name: COLUMN review.target_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.target_type IS '리뷰 대상 종류';


--
-- Name: COLUMN review.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.title IS '제목';


--
-- Name: COLUMN review.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.user_id IS '사용자 ID';


--
-- Name: COLUMN review.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.review.user_type IS '사용자 타입';


--
-- Name: review_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.review ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.review_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    code character varying(255),
    description character varying(255),
    is_admin boolean DEFAULT false NOT NULL,
    is_default boolean DEFAULT false NOT NULL,
    name character varying(255)
);


--
-- Name: COLUMN role.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.role.code IS '역할 코드';


--
-- Name: COLUMN role.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.role.description IS '역할 설명';


--
-- Name: COLUMN role.is_admin; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.role.is_admin IS '관리자 여부';


--
-- Name: COLUMN role.is_default; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.role.is_default IS '기본 역할 여부';


--
-- Name: COLUMN role.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.role.name IS '역할 이름';


--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.role ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: satisfaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.satisfaction (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    five_point integer DEFAULT 0 NOT NULL,
    four_point integer DEFAULT 0 NOT NULL,
    menu_id bigint NOT NULL,
    one_point integer DEFAULT 0 NOT NULL,
    three_point integer DEFAULT 0 NOT NULL,
    two_point integer DEFAULT 0 NOT NULL,
    user_id bigint
);


--
-- Name: COLUMN satisfaction.five_point; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction.five_point IS '5점';


--
-- Name: COLUMN satisfaction.four_point; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction.four_point IS '4점';


--
-- Name: COLUMN satisfaction.menu_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction.menu_id IS '메뉴 ID';


--
-- Name: COLUMN satisfaction.one_point; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction.one_point IS '1점';


--
-- Name: COLUMN satisfaction.three_point; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction.three_point IS '3점';


--
-- Name: COLUMN satisfaction.two_point; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction.two_point IS '2점';


--
-- Name: COLUMN satisfaction.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction.user_id IS '사용자 고유식별번호';


--
-- Name: satisfaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.satisfaction ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.satisfaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: satisfaction_vote; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.satisfaction_vote (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    member_id bigint NOT NULL,
    menu_id bigint NOT NULL,
    opinion character varying(255),
    score integer NOT NULL
);


--
-- Name: COLUMN satisfaction_vote.member_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction_vote.member_id IS '회원 ID';


--
-- Name: COLUMN satisfaction_vote.menu_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction_vote.menu_id IS '메뉴 ID';


--
-- Name: COLUMN satisfaction_vote.opinion; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction_vote.opinion IS '의견';


--
-- Name: COLUMN satisfaction_vote.score; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.satisfaction_vote.score IS '점수 (1~5)';


--
-- Name: satisfaction_vote_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.satisfaction_vote ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.satisfaction_vote_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: site; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.site (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    description character varying(255),
    domain character varying(255),
    home_content_id bigint,
    is_default boolean DEFAULT false NOT NULL,
    locale character varying(255),
    name character varying(255),
    theme character varying(255),
    type character varying(255),
    CONSTRAINT site_type_check CHECK (((type)::text = ANY ((ARRAY['CONTENT'::character varying, 'BOARD'::character varying, 'LINK'::character varying, 'FEATURE'::character varying])::text[])))
);


--
-- Name: COLUMN site.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.site.description IS '사이트 설명';


--
-- Name: COLUMN site.domain; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.site.domain IS '사이트 도메인';


--
-- Name: COLUMN site.home_content_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.site.home_content_id IS '홈 화면으로 쓰는 컨텐츠 ID (없으면 기본 홈)';


--
-- Name: COLUMN site.is_default; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.site.is_default IS '기본 사이트 여부';


--
-- Name: COLUMN site.locale; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.site.locale IS '사이트 로케일';


--
-- Name: COLUMN site.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.site.name IS '사이트 이름';


--
-- Name: COLUMN site.theme; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.site.theme IS '사이트 테마';


--
-- Name: COLUMN site.type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.site.type IS '메뉴 종류';


--
-- Name: site_disabled_feature; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.site_disabled_feature (
    site_id bigint NOT NULL,
    feature character varying(50),
    CONSTRAINT site_disabled_feature_feature_check CHECK (((feature)::text = ANY ((ARRAY['CONTENT'::character varying, 'BOARD'::character varying, 'ARCHIVE'::character varying, 'QNA'::character varying, 'MAP'::character varying, 'PRODUCT'::character varying, 'PROGRAM'::character varying, 'REPORT'::character varying, 'SATISFACTION'::character varying])::text[])))
);


--
-- Name: site_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.site ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.site_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: statistics; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.statistics (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    browser character varying(255),
    count integer NOT NULL,
    date date,
    nation_code character varying(255),
    os character varying(255),
    url character varying(255)
);


--
-- Name: COLUMN statistics.browser; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics.browser IS '브라우저';


--
-- Name: COLUMN statistics.count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics.count IS '방문 횟수';


--
-- Name: COLUMN statistics.date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics.date IS '날짜';


--
-- Name: COLUMN statistics.nation_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics.nation_code IS '방문 국가 코드';


--
-- Name: COLUMN statistics.os; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics.os IS 'OS';


--
-- Name: COLUMN statistics.url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics.url IS 'URL';


--
-- Name: statistics_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.statistics ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.statistics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: statistics_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.statistics_log (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    ip_address character varying(255),
    member_username character varying(255),
    referer character varying(255),
    request_method character varying(255),
    url character varying(255)
);


--
-- Name: COLUMN statistics_log.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics_log.ip_address IS 'IP 주소';


--
-- Name: COLUMN statistics_log.member_username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics_log.member_username IS '회원 아이디';


--
-- Name: COLUMN statistics_log.referer; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics_log.referer IS '리퍼러';


--
-- Name: COLUMN statistics_log.request_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics_log.request_method IS 'HTTP 요청 메서드';


--
-- Name: COLUMN statistics_log.url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.statistics_log.url IS '방문 URL';


--
-- Name: statistics_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.statistics_log ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.statistics_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: watchdog_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.watchdog_log (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    modified_at timestamp(6) without time zone,
    content character varying(255),
    ip_address character varying(255),
    member_username character varying(255),
    method_name character varying(255),
    uri character varying(255)
);


--
-- Name: COLUMN watchdog_log.content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.watchdog_log.content IS '로그 내용';


--
-- Name: COLUMN watchdog_log.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.watchdog_log.ip_address IS 'IP 주소';


--
-- Name: COLUMN watchdog_log.member_username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.watchdog_log.member_username IS '회원 아이디';


--
-- Name: COLUMN watchdog_log.method_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.watchdog_log.method_name IS '메서드명';


--
-- Name: COLUMN watchdog_log.uri; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.watchdog_log.uri IS '요청 URI';


--
-- Name: watchdog_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.watchdog_log ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.watchdog_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: admin; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.admin (id, created_at, deleted_at, modified_at, birth_date, email, first_name, gender, group_id, is_lunar_birth_date, last_login_date_time, last_name, login_try_count, mobile_phone_name, name, password, role_id, telephone_name, term_confirmation_date, term_expiration_date, username) FROM stdin;
1	2026-11-02 20:08:41.222334	\N	2026-11-02 20:08:42.461754	\N	\N	\N	\N	\N	f	2026-11-02 20:08:42.461438	\N	0	\N	관리자	{bcrypt}$2a$10$9/p9.YnYmHrehswmeROGAu8Sp9HLxC3TYLk5vHOBH6yincZpN2K5W	\N	\N	\N	\N	admin
\.


--
-- Data for Name: admin_address; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.admin_address (id, address, admin_id, detail_address, postal_code) FROM stdin;
\.


--
-- Data for Name: admin_organization; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.admin_organization (id, member_id, office_address, office_department, office_detail_address, office_name, office_position) FROM stdin;
\.


--
-- Data for Name: answer; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.answer (id, created_at, deleted_at, modified_at, author_name, content_html, content_text, question_ref_id, user_id, user_type) FROM stdin;
1	2026-11-02 20:08:45.023188	\N	2026-11-02 20:08:45.023188	관리자	<p>검수센터 도착 후 영업일 기준 1~2일 안에 검수를 마치고 바로 판매 등록됩니다.</p>	검수센터 도착 후 영업일 기준 1~2일 안에 검수를 마치고 바로 판매 등록됩니다.	1	1	ADMIN
2	2026-11-02 20:08:45.040495	\N	2026-11-02 20:08:45.040495	관리자	<p>불편을 드려 죄송합니다. 수령 후 7일 이내라 무료 반품이 가능합니다. 마이페이지 주문 내역에서 반품을 신청해 주세요.</p>	불편을 드려 죄송합니다. 수령 후 7일 이내라 무료 반품이 가능합니다. 마이페이지 주문 내역에서 반품을 신청해 주세요.	2	1	ADMIN
\.


--
-- Data for Name: archive; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.archive (id, created_at, deleted_at, modified_at, description, fields, name, page_size, supports_comment, type) FROM stdin;
1	2026-11-02 20:08:42.883145	\N	2026-11-02 20:08:42.883145	중고천국 소식과 운영 안내	\N	공지사항	10	f	NORMAL
2	2026-11-02 20:08:43.855892	\N	2026-11-02 20:08:43.855892	거래에 필요한 양식과 가이드	\N	거래 자료실	10	t	NORMAL
\.


--
-- Data for Name: archive_article; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.archive_article (id, created_at, deleted_at, modified_at, board_id, author_name, content_html, content_text, fields, hit, ip_address, likes, status, title, user_id, user_type) FROM stdin;
1	2026-11-02 20:08:42.900854	\N	2026-11-02 20:08:42.900854	1	관리자	<p>최근 외부 메신저로 유도해 선입금을 요구하는 사례가 늘고 있습니다. 안전결제 외의 송금 요구에는 응하지 마세요.</p>	최근 외부 메신저로 유도해 선입금을 요구하는 사례가 늘고 있습니다. 안전결제 외의 송금 요구에는 응하지 마세요.	\N	0	0:0:0:0:0:0:0:1	0	PUBLISHED	사기 거래 주의 안내	1	ADMIN
2	2026-11-02 20:08:42.913344	\N	2026-11-02 20:08:42.913344	1	관리자	<p>12월부터 검수센터가 토요일에도 운영합니다 (10:00~15:00).</p>	12월부터 검수센터가 토요일에도 운영합니다 (10:00~15:00).	\N	0	0:0:0:0:0:0:0:1	0	PUBLISHED	검수센터 운영 시간 변경	1	ADMIN
3	2026-11-02 20:08:42.921317	\N	2026-11-02 20:08:42.921317	1	관리자	<p>서비스 안정화를 위한 정기 점검이 진행됩니다. 점검 중에는 주문과 결제가 일시 중단됩니다.</p>	서비스 안정화를 위한 정기 점검이 진행됩니다. 점검 중에는 주문과 결제가 일시 중단됩니다.	\N	0	0:0:0:0:0:0:0:1	0	PUBLISHED	11월 정기 점검 안내 (11/12 02:00~05:00)	1	ADMIN
4	2026-11-02 20:08:42.930975	\N	2026-11-02 20:08:42.930975	1	관리자	<p>중고천국이 정식으로 문을 열었습니다. 오픈 기념으로 첫 구매 10% 할인 쿠폰을 드립니다.</p>	중고천국이 정식으로 문을 열었습니다. 오픈 기념으로 첫 구매 10% 할인 쿠폰을 드립니다.	\N	0	0:0:0:0:0:0:0:1	0	PUBLISHED	중고천국 정식 오픈 안내	1	ADMIN
5	2026-11-02 20:08:42.941163	\N	2026-11-02 20:08:42.941163	1	관리자	<p>12월 중순 공개 예정입니다.</p>	12월 중순 공개 예정입니다.	\N	0	0:0:0:0:0:0:0:1	0	DRAFT	연말 감사 이벤트 예고	1	ADMIN
6	2026-11-02 20:08:43.863571	\N	2026-11-02 20:08:43.863571	2	관리자	<p>직거래 시 작성하는 표준 계약서 양식입니다. 인쇄해서 거래 당일에 지참하세요.</p>	직거래 시 작성하는 표준 계약서 양식입니다. 인쇄해서 거래 당일에 지참하세요.	\N	0	0:0:0:0:0:0:0:1	0	PUBLISHED	중고거래 표준 계약서 양식	1	ADMIN
7	2026-11-02 20:08:43.872889	\N	2026-11-02 20:08:43.872889	2	관리자	<p>자연광, 흰 배경, 하자 부위 근접 촬영 — 세 가지만 지켜도 판매 속도가 두 배 빨라집니다.</p>	자연광, 흰 배경, 하자 부위 근접 촬영 — 세 가지만 지켜도 판매 속도가 두 배 빨라집니다.	\N	0	0:0:0:0:0:0:0:1	0	PUBLISHED	상품 사진 잘 찍는 법	1	ADMIN
8	2026-11-02 20:08:43.882909	\N	2026-11-02 20:08:43.882909	2	관리자	<p>S/A/B/C 등급별 외관·기능 기준을 정리했습니다.</p>	S/A/B/C 등급별 외관·기능 기준을 정리했습니다.	\N	0	0:0:0:0:0:0:0:1	0	PUBLISHED	검수 등급 기준표 (2026년 개정)	1	ADMIN
\.


--
-- Data for Name: archive_article_file; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.archive_article_file (id, article_id, file_id, sequence) FROM stdin;
\.


--
-- Data for Name: archive_comment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.archive_comment (id, created_at, deleted_at, modified_at, article_id, author_name, content, depth, hit, ip_address, "order", parent_id, profile_image_file_id, sns_type, sns_user_url, sns_username, status, title, user_id, user_type) FROM stdin;
\.


--
-- Data for Name: authenticated_guest; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.authenticated_guest (connection_info, duplication_info, id) FROM stdin;
\.


--
-- Data for Name: authenticated_member; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.authenticated_member (connection_info, duplication_info, id) FROM stdin;
\.


--
-- Data for Name: banner; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.banner (id, created_at, deleted_at, modified_at, background_color, description, dismiss_days, end_date_time, file_id, height, is_modal, is_using, "left", link, name, opens_new_window, "order", start_date_time, text_color, top, type, width) FROM stdin;
1	2026-11-02 20:08:45.326049	\N	2026-11-02 20:08:45.326049	#0F766E	🎉 중고천국 정식 오픈! 첫 구매 10% 할인 쿠폰을 드려요	1	\N	\N	\N	f	t	\N	/products	오픈 이벤트 띠배너	f	0	\N	#FFFFFF	\N	BANNER	\N
2	2026-11-02 20:08:45.3384	\N	2026-11-02 20:08:45.3384	#B45309	11/12(목) 02:00~05:00 정기 점검이 있습니다.	1	\N	\N	\N	f	f	\N	\N	점검 안내 (대기)	f	1	\N	#FFFFFF	\N	BANNER	\N
3	2026-11-02 20:08:45.371668	\N	2026-11-02 20:08:45.371668	\N	성수 주말 플리마켓 셀러를 모집합니다. 부스비 2만 원, 판매 수수료 0원!	3	\N	13	\N	t	t	\N	/programs/1	플리마켓 셀러 모집	f	0	\N	\N	\N	POPUP	420
4	2026-11-02 20:08:45.377457	\N	2026-11-02 20:08:45.377457	\N	외부 메신저·계좌이체 요구는 100% 사기입니다.\n반드시 중고천국 안전결제를 이용하세요.	7	\N	\N	\N	t	t	\N	/contents/3	안전결제 안내	f	1	\N	\N	\N	POPUP	380
\.


--
-- Data for Name: board; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board (id, created_at, deleted_at, modified_at, description, fields, name, type) FROM stdin;
1	2026-11-02 20:08:42.853385	\N	2026-11-02 20:08:42.853385	중고천국에서 사고판 이야기	\N	거래 후기	NORMAL
2	2026-11-02 20:08:42.868152	\N	2026-11-02 20:08:42.868152	중고 이야기 무엇이든	\N	자유게시판	NORMAL
\.


--
-- Data for Name: board_article; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_article (id, created_at, deleted_at, modified_at, author_name, board_id, content_html, content_text, fields, hit, ip_address, is_hidden, is_notice, is_secret, likes, notice_end_date_time, notice_start_date_time, password, title, user_id, user_type) FROM stdin;
2	2026-11-02 20:08:43.678523	\N	2026-11-02 20:08:43.678523	박서연	1	<p>대전 검수센터에서 픽업했어요. 직원분이 펼치는 법까지 알려주셔서 좋았습니다.</p>	대전 검수센터에서 픽업했어요. 직원분이 펼치는 법까지 알려주셔서 좋았습니다.	\N	0	0:0:0:0:0:0:0:1	f	f	f	0	\N	\N	\N	캠핑 의자 직거래 후기	3	MEMBER
3	2026-11-02 20:08:43.687596	\N	2026-11-02 20:08:43.687596	이도윤	1	<p>검수 신청하고 사흘 만에 판매 완료됐어요. 수수료도 생각보다 저렴합니다.</p>	검수 신청하고 사흘 만에 판매 완료됐어요. 수수료도 생각보다 저렴합니다.	\N	0	0:0:0:0:0:0:0:1	f	f	f	0	\N	\N	\N	게임기 팔아봤습니다	2	MEMBER
1	2026-11-02 20:08:43.667116	\N	2026-11-02 20:08:43.721513	김하늘	1	<p>A급이라고 해서 반신반의했는데 정말 흠집 하나 없네요. 배터리 성능도 상세 페이지와 같았습니다.</p>	A급이라고 해서 반신반의했는데 정말 흠집 하나 없네요. 배터리 성능도 상세 페이지와 같았습니다.	\N	0	0:0:0:0:0:0:0:1	f	f	f	2	\N	\N	\N	아이폰 검수 상태 그대로 왔어요	1	MEMBER
4	2026-11-02 20:08:43.753182	\N	2026-11-02 20:08:43.753182	최민준	2	<p>배터리 사이클 수, 힌지 유격, 키보드 눌림은 꼭 확인하세요. 검수 리포트에 다 나와서 편하더라고요.</p>	배터리 사이클 수, 힌지 유격, 키보드 눌림은 꼭 확인하세요. 검수 리포트에 다 나와서 편하더라고요.	\N	0	0:0:0:0:0:0:0:1	f	f	f	0	\N	\N	\N	중고 노트북 고를 때 꼭 보는 것	4	MEMBER
5	2026-11-02 20:08:43.842789	\N	2026-11-02 20:08:43.842789	지나가던사람	2	<p>구경만 하다가 처음 글 남겨봐요.</p>	구경만 하다가 처음 글 남겨봐요.	\N	0	0:0:0:0:0:0:0:1	f	f	f	0	\N	\N	{bcrypt}$2a$10$gGOINMni5567iffEmNGSHey.j8zYnHKeXLD0EKjMRJUfKX2uMcsuu	비회원도 글 쓸 수 있네요	\N	GUEST
6	2026-11-02 20:08:43.850203	\N	2026-11-02 20:08:43.850203	이도윤	2	<p>직거래 장소를 바꿀 수 있을까요?</p>	직거래 장소를 바꿀 수 있을까요?	\N	0	0:0:0:0:0:0:0:1	f	f	t	0	\N	\N	\N	판매자에게만 보이는 질문 (비밀글)	2	MEMBER
\.


--
-- Data for Name: board_article_file; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_article_file (id, article_id, file_id, sequence) FROM stdin;
\.


--
-- Data for Name: board_article_like; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_article_like (id, article_id, member_id) FROM stdin;
1	1	1
2	1	4
\.


--
-- Data for Name: board_comment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_comment (id, created_at, deleted_at, modified_at, article_id, author_name, content, depth, hit, ip_address, "order", parent_id, password, profile_image_file_id, sns_type, sns_user_url, sns_username, status, title, user_id, user_type) FROM stdin;
1	2026-11-02 20:08:43.736404	\N	2026-11-02 20:08:43.736404	1	최민준	저도 같은 판매자한테 샀는데 만족했어요!	0	0	0:0:0:0:0:0:0:1	0	\N	\N	\N	\N	\N	\N	APPROVED	\N	4	MEMBER
2	2026-11-02 20:08:43.747024	\N	2026-11-02 20:08:43.747024	1	김하늘	역시 검수 받은 게 마음 편하네요 :)	0	0	0:0:0:0:0:0:0:1	0	\N	\N	\N	\N	\N	\N	APPROVED	\N	1	MEMBER
\.


--
-- Data for Name: board_config; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.board_config (id, created_at, deleted_at, modified_at, board_id, is_guest_accessible, is_guest_readable, is_guest_writable, is_member_accessible, is_member_readable, is_member_writable, maximum_file_count, page_size, supports_attachment, supports_comment, supports_like, supports_notice, supports_secret) FROM stdin;
1	2026-11-02 20:08:42.856512	\N	2026-11-02 20:08:42.856512	1	t	t	f	t	t	t	5	10	t	t	t	t	f
2	2026-11-02 20:08:42.869017	\N	2026-11-02 20:08:42.869017	2	t	t	t	t	t	t	5	10	t	t	t	t	t
\.


--
-- Data for Name: branch; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.branch (id, created_at, deleted_at, modified_at, address, business_hours, description, detail_address, is_using, latitude, longitude, name, phone_number, sequence) FROM stdin;
1	2026-11-02 20:08:43.923547	\N	2026-11-02 20:08:43.923547	서울특별시 강남구 강남대로 396	매일 10:00~20:00	디지털 기기 전문 검수, 당일 매입 상담	지하 1층	t	37.4979000	127.0276000	강남 검수센터	02-555-0101	0
2	2026-11-02 20:08:43.935583	\N	2026-11-02 20:08:43.935583	서울특별시 성동구 아차산로 17	화~일 11:00~21:00	가구·생활용품 직접 보고 구매	1층	t	37.5446000	127.0557000	성수 픽업라운지	02-555-0102	1
3	2026-11-02 20:08:43.946614	\N	2026-11-02 20:08:43.946614	부산광역시 부산진구 중앙대로 692	매일 10:00~19:00	부산·경남 지역 매입과 픽업	2층	t	35.1578000	129.0600000	부산 서면점	051-555-0103	2
4	2026-11-02 20:08:43.954804	\N	2026-11-02 20:08:43.954804	대전광역시 서구 대덕대로 211	월~토 10:00~19:00	캠핑·레저 용품 특화 매장	1층	t	36.3510000	127.3850000	대전 둔산점	042-555-0104	3
\.


--
-- Data for Name: capability; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.capability (id, created_at, deleted_at, modified_at, description, is_default, name, priority, user_id, user_type) FROM stdin;
\.


--
-- Data for Name: code; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.code (id, created_at, deleted_at, modified_at, depth, description, english_name, is_using, name, "order", parent_id) FROM stdin;
1	2026-11-02 20:08:42.644906	\N	2026-11-02 20:08:42.644906	0	\N	GRADE	t	상품 등급	0	\N
2	2026-11-02 20:08:42.65815	\N	2026-11-02 20:08:42.65815	1	\N	S	t	S급 (미개봉)	0	1
3	2026-11-02 20:08:42.665984	\N	2026-11-02 20:08:42.665984	1	\N	A	t	A급 (사용감 거의 없음)	1	1
4	2026-11-02 20:08:42.674576	\N	2026-11-02 20:08:42.674576	1	\N	B	t	B급 (생활 흠집)	2	1
5	2026-11-02 20:08:42.681796	\N	2026-11-02 20:08:42.681796	1	\N	C	t	C급 (기능 정상, 외관 하자)	3	1
6	2026-11-02 20:08:42.687361	\N	2026-11-02 20:08:42.687361	0	\N	TRADE	t	거래 방식	1	\N
7	2026-11-02 20:08:42.69529	\N	2026-11-02 20:08:42.69529	1	\N	DELIVERY	t	택배 거래	0	6
8	2026-11-02 20:08:42.702369	\N	2026-11-02 20:08:42.702369	1	\N	DIRECT	t	직거래	1	6
9	2026-11-02 20:08:42.70969	\N	2026-11-02 20:08:42.70969	1	\N	PICKUP	t	매장 픽업	2	6
\.


--
-- Data for Name: config; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.config (id, created_at, deleted_at, modified_at, description, is_hidden, key, type, value) FROM stdin;
1	2026-11-02 20:08:42.593858	\N	2026-11-02 20:08:42.593858	사이트 하단 문구	f	site.footer	TEXT	ⓒ 2026 중고천국 · 사업자등록번호 000-00-00000 · 고객센터 1588-0000 (평일 09:00~18:00)
2	2026-11-02 20:08:42.60177	\N	2026-11-02 20:08:42.60177	고객센터 대표 번호	f	site.contact	TEXT	1588-0000
\.


--
-- Data for Name: content; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.content (id, created_at, deleted_at, modified_at, content_html, content_text, last_worker, memo, status, title, version) FROM stdin;
1	2026-11-02 20:08:42.727191	\N	2026-11-02 20:08:42.727191	<section class="hero"><h1>버리기 아까운 물건에, 두 번째 주인을</h1><p class="hero-description">전문 검수와 안전결제로 믿고 사고파는 중고천국</p><div data-block="product-search"></div><div data-block="greeting"></div></section><div data-block="shortcuts"></div><div data-block="new-products"></div><section class="home-section"><div class="section-head"><h2>중고천국이 다른 이유</h2></div><div class="feature-grid"><div class="feature-card"><h3>&#x1f50d; 전문 검수</h3><p>검수센터에서 기능·외관을 점검하고 S/A/B/C 등급을 매깁니다.</p></div><div class="feature-card"><h3>&#x1f512; 안전결제</h3><p>구매 확정 전까지 대금을 중고천국이 안전하게 보관합니다.</p></div><div class="feature-card"><h3>↩️ 7일 환불</h3><p>검수 내용과 다르면 수령 후 7일 안에 환불해 드립니다.</p></div></div></section><div class="home-columns"><div data-block="open-programs"></div><div data-block="latest-posts"></div></div>	버리기 아까운 물건에, 두 번째 주인을 전문 검수와 안전결제로 믿고 사고파는 중고천국 중고천국이 다른 이유 &#x1f50d; 전문 검수 검수센터에서 기능·외관을 점검하고 S/A/B/C 등급을 매깁니다. &#x1f512; 안전결제 구매 확정 전까지 대금을 중고천국이 안전하게 보관합니다. ↩️ 7일 환불 검수 내용과 다르면 수령 후 7일 안에 환불해 드립니다.	admin	사이트 설정의 홈 화면 컨텐츠로 지정됨	PUBLISHED	홈 (랜딩 페이지)	1
2	2026-11-02 20:08:42.752274	\N	2026-11-02 20:08:42.752274	<h2>버리기 아까운 물건에 두 번째 주인을</h2><p>중고천국은 2026년에 문을 연 검수형 중고거래 플랫폼입니다. 전문 검수사가 상품 상태를 직접 확인하고 등급을 매겨, 사진만 보고 사는 중고거래의 불안을 줄였습니다.</p><ul><li><strong>전문 검수</strong> — 모든 상품을 검수센터에서 기능·외관 점검 후 S/A/B/C 등급으로 표시합니다.</li><li><strong>안전결제</strong> — 구매 확정 전까지 대금을 중고천국이 보관합니다.</li><li><strong>7일 환불</strong> — 검수 내용과 다르면 수령 후 7일 안에 환불해 드립니다.</li></ul>	버리기 아까운 물건에 두 번째 주인을 중고천국은 2026년에 문을 연 검수형 중고거래 플랫폼입니다. 전문 검수사가 상품 상태를 직접 확인하고 등급을 매겨, 사진만 보고 사는 중고거래의 불안을 줄였습니다. 전문 검수 — 모든 상품을 검수센터에서 기능·외관 점검 후 S/A/B/C 등급으로 표시합니다. 안전결제 — 구매 확정 전까지 대금을 중고천국이 보관합니다. 7일 환불 — 검수 내용과 다르면 수령 후 7일 안에 환불해 드립니다.	admin		PUBLISHED	중고천국 소개	1
3	2026-11-02 20:08:42.761685	\N	2026-11-02 20:08:42.761685	<h2>안전한 중고거래를 위한 5가지 약속</h2><ol><li>판매자가 외부 메신저나 계좌 이체를 요구하면 거래를 중단하세요.</li><li>직거래는 사람이 많은 공공장소에서, 물건을 확인한 뒤 결제하세요.</li><li>시세보다 지나치게 싼 상품은 한 번 더 의심하세요.</li><li>택배 거래는 반드시 중고천국 안전결제를 이용하세요.</li><li>사기가 의심되면 즉시 <a href="/reports/new" rel="nofollow">사기 의심 신고</a>를 접수해 주세요.</li></ol>	안전한 중고거래를 위한 5가지 약속 판매자가 외부 메신저나 계좌 이체를 요구하면 거래를 중단하세요. 직거래는 사람이 많은 공공장소에서, 물건을 확인한 뒤 결제하세요. 시세보다 지나치게 싼 상품은 한 번 더 의심하세요. 택배 거래는 반드시 중고천국 안전결제를 이용하세요. 사기가 의심되면 즉시 사기 의심 신고 를 접수해 주세요.	admin		PUBLISHED	안전거래 가이드	1
4	2026-11-02 20:08:42.770448	\N	2026-11-02 20:08:42.770448	<h2>판매 수수료</h2><table><thead><tr><th>판매 금액</th><th>수수료</th></tr></thead><tbody><tr><td>10만 원 미만</td><td>5%</td></tr><tr><td>10만 원 ~ 100만 원</td><td>3.5%</td></tr><tr><td>100만 원 이상</td><td>2.5%</td></tr></tbody></table><p>검수비는 무료이며, 검수에서 반려된 상품은 왕복 택배비만 부담합니다.</p>	판매 수수료 판매 금액 수수료 10만 원 미만 5% 10만 원 ~ 100만 원 3.5% 100만 원 이상 2.5% 검수비는 무료이며, 검수에서 반려된 상품은 왕복 택배비만 부담합니다.	admin		PUBLISHED	판매 수수료 안내	1
5	2026-11-02 20:08:42.779665	\N	2026-11-02 20:08:42.779665	<h2>사진 출처</h2><p>샘플 상품·프로그램 사진은 위키미디어 공용의 자유 라이선스 이미지를 축소해 사용했습니다.</p><ul><li><a href="https://commons.wikimedia.org/wiki/File:Back_view_of_iPhone_15_Pro_Natural_titanium.jpg" rel="nofollow">Back view of iPhone 15 Pro Natural titanium.jpg</a> — メイド理世, <a href="https://creativecommons.org/licenses/by-sa/4.0" rel="nofollow">CC BY-SA 4.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:M2_Macbook_Air_Starlight_model.jpg" rel="nofollow">M2 Macbook Air Starlight model.jpg</a> — KKPCW&#xff08;Kyu3&#xff09;, <a href="https://creativecommons.org/licenses/by-sa/4.0" rel="nofollow">CC BY-SA 4.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:Nintendo-Switch-Console-Docked-wJoyConRB.jpg" rel="nofollow">Nintendo-Switch-Console-Docked-wJoyConRB.jpg</a> — Evan-Amos, Public domain</li><li><a href="https://commons.wikimedia.org/wiki/File:Dyson_Cyclone_V10_Absolute_cordless_stick_vacuum.jpg" rel="nofollow">Dyson Cyclone V10 Absolute cordless stick vacuum.jpg</a> — MattClayt, <a href="https://creativecommons.org/licenses/by-sa/4.0" rel="nofollow">CC BY-SA 4.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:Helinox_Ground_Chair.jpg" rel="nofollow">Helinox Ground Chair.jpg</a> — PsamatheM, <a href="https://creativecommons.org/licenses/by-sa/4.0" rel="nofollow">CC BY-SA 4.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:LG_GRAM_16Z90R.jpg" rel="nofollow">LG GRAM 16Z90R.jpg</a> — Dinkun Chen, <a href="https://creativecommons.org/licenses/by-sa/4.0" rel="nofollow">CC BY-SA 4.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:Sony-WH-1000XM3-kabellose-Bluetooth-Noise-Cancelling-Kopfhoerer.jpg" rel="nofollow">Sony-WH-1000XM3-kabellose-Bluetooth-Noise-Cancelling-Kopfhoerer.jpg</a> — www.digitalpush.net, <a href="https://creativecommons.org/licenses/by/4.0" rel="nofollow">CC BY 4.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:PO%C3%84NG_in_%E2%80%9CKnisa_Beige%E2%80%9D_%2845144076894%29.jpg" rel="nofollow">POÄNG in “Knisa Beige” (45144076894).jpg</a> — Paulo O from Halifax, Canada, <a href="https://creativecommons.org/licenses/by/2.0" rel="nofollow">CC BY 2.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:Samsung_Galaxy_Z_Flip_4.jpg" rel="nofollow">Samsung Galaxy Z Flip 4.jpg</a> — Hajoon0102, <a href="https://creativecommons.org/licenses/by-sa/4.0" rel="nofollow">CC BY-SA 4.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:Flea_market_at_Yurakucho%27s_Tokyo_International_Forum.jpg" rel="nofollow">Flea market at Yurakucho&#39;s Tokyo International Forum.jpg</a> — Syced, <a href="http://creativecommons.org/publicdomain/zero/1.0/deed.en" rel="nofollow">CC0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:Laptop_Repair.jpg" rel="nofollow">Laptop Repair.jpg</a> — Augustin Wafflart, <a href="https://creativecommons.org/licenses/by/4.0" rel="nofollow">CC BY 4.0</a></li><li><a href="https://commons.wikimedia.org/wiki/File:Camping-Chair_001.jpg" rel="nofollow">Camping-Chair 001.jpg</a> — Ocdp, <a href="http://creativecommons.org/publicdomain/zero/1.0/deed.en" rel="nofollow">CC0</a></li></ul>	사진 출처 샘플 상품·프로그램 사진은 위키미디어 공용의 자유 라이선스 이미지를 축소해 사용했습니다. Back view of iPhone 15 Pro Natural titanium.jpg — メイド理世, CC BY-SA 4.0 M2 Macbook Air Starlight model.jpg — KKPCW&#xff08;Kyu3&#xff09;, CC BY-SA 4.0 Nintendo-Switch-Console-Docked-wJoyConRB.jpg — Evan-Amos, Public domain Dyson Cyclone V10 Absolute cordless stick vacuum.jpg — MattClayt, CC BY-SA 4.0 Helinox Ground Chair.jpg — PsamatheM, CC BY-SA 4.0 LG GRAM 16Z90R.jpg — Dinkun Chen, CC BY-SA 4.0 Sony-WH-1000XM3-kabellose-Bluetooth-Noise-Cancelling-Kopfhoerer.jpg — www.digitalpush.net, CC BY 4.0 POÄNG in “Knisa Beige” (45144076894).jpg — Paulo O from Halifax, Canada, CC BY 2.0 Samsung Galaxy Z Flip 4.jpg — Hajoon0102, CC BY-SA 4.0 Flea market at Yurakucho's Tokyo International Forum.jpg — Syced, CC0 Laptop Repair.jpg — Augustin Wafflart, CC BY 4.0 Camping-Chair 001.jpg — Ocdp, CC0	admin		PUBLISHED	사진 출처	1
6	2026-11-02 20:08:42.790629	\N	2026-11-02 20:08:42.790629	<p>검수사 채용 공고를 준비하고 있습니다.</p>	검수사 채용 공고를 준비하고 있습니다.	admin	공개 전 인사팀 검토 필요	DRAFT	채용 안내 (준비 중)	1
\.


--
-- Data for Name: discount; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.discount (id, created_at, deleted_at, modified_at, description, is_using, name, product_id, rate) FROM stdin;
1	2026-11-02 20:08:44.569208	\N	2026-11-02 20:08:44.569208	모든 상품 첫 구매 할인	t	오픈 기념 10% 할인	\N	10.00
2	2026-11-02 20:08:44.581795	\N	2026-11-02 20:08:44.581795	닌텐도 스위치 한정 15%	t	스위치 주말 특가	3	15.00
3	2026-11-02 20:08:44.590978	\N	2026-11-02 20:08:44.590978	지난 이벤트	f	추석 감사 할인 (종료)	\N	20.00
\.


--
-- Data for Name: file; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.file (id, created_at, deleted_at, modified_at, alternative_text, download_count, extension, media_type, mime_type, original_name, path, size, uuid) FROM stdin;
1	2026-11-02 20:08:44.128498	\N	2026-11-02 20:08:44.128498	아이폰 15 Pro 256GB 내추럴 티타늄	0	jpg	IMAGE	image/jpeg	iphone-15-pro.jpg	2026/11/02/c1db504c-b554-4871-bba3-d2651b73b055.jpg	214804	c1db504c-b554-4871-bba3-d2651b73b055
2	2026-11-02 20:08:44.219226	\N	2026-11-02 20:08:44.219226	맥북 에어 M2 13인치 스타라이트 16GB/512GB	0	jpg	IMAGE	image/jpeg	macbook-air-m2.jpg	2026/11/02/bae03102-4d0e-420f-aa6d-008fcf243aec.jpg	146632	bae03102-4d0e-420f-aa6d-008fcf243aec
3	2026-11-02 20:08:44.256914	\N	2026-11-02 20:08:44.256914	닌텐도 스위치 네온 블루/레드	0	jpg	IMAGE	image/jpeg	nintendo-switch.jpg	2026/11/02/dfbc6a0d-c464-45c6-8f71-9e1ba35edf06.jpg	49013	dfbc6a0d-c464-45c6-8f71-9e1ba35edf06
4	2026-11-02 20:08:44.29556	\N	2026-11-02 20:08:44.29556	다이슨 V10 앱솔루트 무선청소기	0	jpg	IMAGE	image/jpeg	dyson-v10.jpg	2026/11/02/0e8bddee-95d0-452b-b5e9-5c8f5ccc6113.jpg	80634	0e8bddee-95d0-452b-b5e9-5c8f5ccc6113
5	2026-11-02 20:08:44.346521	\N	2026-11-02 20:08:44.346521	헬리녹스 그라운드 체어 2개 세트	0	jpg	IMAGE	image/jpeg	helinox-ground-chair.jpg	2026/11/02/7fab5934-5c6b-4679-ae27-d64c363c48e4.jpg	297935	7fab5934-5c6b-4679-ae27-d64c363c48e4
6	2026-11-02 20:08:44.398912	\N	2026-11-02 20:08:44.398912	LG 그램 16 [2023) i7/16GB	0	jpg	IMAGE	image/jpeg	lg-gram-16.jpg	2026/11/02/8822f930-0107-48e9-8dbc-00c8ecc754ac.jpg	152506	8822f930-0107-48e9-8dbc-00c8ecc754ac
7	2026-11-02 20:08:44.442648	\N	2026-11-02 20:08:44.442648	소니 WH-1000XM3 블랙	0	jpg	IMAGE	image/jpeg	sony-wh-1000xm3.jpg	2026/11/02/1c16e69e-4a6b-4308-80f3-d5a6748122fd.jpg	229631	1c16e69e-4a6b-4308-80f3-d5a6748122fd
8	2026-11-02 20:08:44.489087	\N	2026-11-02 20:08:44.489087	이케아 포엥 암체어 [쿠션 포함)	0	jpg	IMAGE	image/jpeg	ikea-poang.jpg	2026/11/02/0bc55797-7273-4917-b133-49c5ac7ba358.jpg	226443	0bc55797-7273-4917-b133-49c5ac7ba358
9	2026-11-02 20:08:44.543042	\N	2026-11-02 20:08:44.543042	갤럭시 Z 플립4	0	jpg	IMAGE	image/jpeg	galaxy-z-flip4.jpg	2026/11/02/9861a67e-df8b-4746-9fb5-0e839d00e63b.jpg	115074	9861a67e-df8b-4746-9fb5-0e839d00e63b
10	2026-11-02 20:08:44.801658	\N	2026-11-02 20:08:44.801658	주말 플리마켓 셀러 모집 [성수)	0	jpg	IMAGE	image/jpeg	flea-market.jpg	2026/11/02/0aa7135a-5479-4f60-a76b-0de57b2b0cc3.jpg	311380	0aa7135a-5479-4f60-a76b-0de57b2b0cc3
11	2026-11-02 20:08:44.86497	\N	2026-11-02 20:08:44.86497	중고 노트북 셀프 점검 클래스	0	jpg	IMAGE	image/jpeg	laptop-repair.jpg	2026/11/02/22b06b5b-5e9c-4b8f-aa19-60fca77c96a4.jpg	482128	22b06b5b-5e9c-4b8f-aa19-60fca77c96a4
12	2026-11-02 20:08:44.90615	\N	2026-11-02 20:08:44.90615	캠핑용품 업사이클링 워크숍 [대전)	0	jpg	IMAGE	image/jpeg	camping-chair.jpg	2026/11/02/a56dd276-610d-428e-923d-b265ffc104aa.jpg	259513	a56dd276-610d-428e-923d-b265ffc104aa
13	2026-11-02 20:08:45.366056	\N	2026-11-02 20:08:45.366056	플리마켓 셀러 모집	0	jpg	IMAGE	image/jpeg	flea-market.jpg	2026/11/02/b7cb1bc6-da6b-4ba4-9106-6048ea172dbf.jpg	311380	b7cb1bc6-da6b-4ba4-9106-6048ea172dbf
\.


--
-- Data for Name: group; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public."group" (id, created_at, deleted_at, modified_at, depth, description, name, "order", parent_id) FROM stdin;
\.


--
-- Data for Name: guest; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.guest (id, created_at, deleted_at, modified_at, birth_date, email, gender, ip_address, mobile_phone_name, name, telephone_name) FROM stdin;
\.


--
-- Data for Name: image_file; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.image_file (original_height, original_width, thumbnail_height, thumbnail_path, thumbnail_width, id) FROM stdin;
1280	960	320	2026/11/02/c1db504c-b554-4871-bba3-d2651b73b055_thumb.png	240	1
960	960	320	2026/11/02/bae03102-4d0e-420f-aa6d-008fcf243aec_thumb.png	320	2
549	960	183	2026/11/02/dfbc6a0d-c464-45c6-8f71-9e1ba35edf06_thumb.png	320	3
640	960	213	2026/11/02/0e8bddee-95d0-452b-b5e9-5c8f5ccc6113_thumb.png	320	4
640	960	213	2026/11/02/7fab5934-5c6b-4679-ae27-d64c363c48e4_thumb.png	320	5
718	960	239	2026/11/02/8822f930-0107-48e9-8dbc-00c8ecc754ac_thumb.png	320	6
1280	960	320	2026/11/02/1c16e69e-4a6b-4308-80f3-d5a6748122fd_thumb.png	240	7
943	960	314	2026/11/02/0bc55797-7273-4917-b133-49c5ac7ba358_thumb.png	320	8
1280	960	320	2026/11/02/9861a67e-df8b-4746-9fb5-0e839d00e63b_thumb.png	240	9
720	960	240	2026/11/02/0aa7135a-5479-4f60-a76b-0de57b2b0cc3_thumb.png	320	10
1280	960	320	2026/11/02/22b06b5b-5e9c-4b8f-aa19-60fca77c96a4_thumb.png	240	11
638	960	213	2026/11/02/a56dd276-610d-428e-923d-b265ffc104aa_thumb.png	320	12
720	960	240	2026/11/02/b7cb1bc6-da6b-4ba4-9106-6048ea172dbf_thumb.png	320	13
\.


--
-- Data for Name: map_setting; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.map_setting (id, created_at, deleted_at, modified_at, branches_enabled, directions_enabled, kakao_app_key, naver_client_id, office_address, office_business_hours, office_description, office_detail_address, office_latitude, office_longitude, office_name, office_phone_number, provider) FROM stdin;
1	2026-11-02 20:08:43.903451	\N	2026-11-02 20:08:43.906556	t	t	\N	\N	서울특별시 강남구 테헤란로 152	평일 09:00~18:00 (점심 12:00~13:00)	2호선 강남역 1번 출구에서 도보 5분입니다. 건물 지하 주차장을 1시간 무료로 이용할 수 있습니다.	중고천국빌딩 5층	37.5000000	127.0364000	중고천국 본사	1588-0000	KAKAO
\.


--
-- Data for Name: member; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.member (id, created_at, deleted_at, modified_at, birth_date, email, first_name, gender, group_id, is_lunar_birth_date, last_login_date_time, last_name, login_try_count, mobile_phone_number, name, password, role_id, telephone_number, term_confirmation_date, term_expiration_date, username) FROM stdin;
1	2026-11-02 20:08:43.043912	\N	2026-11-02 20:08:43.131604	\N	sky@example.com	\N	\N	\N	f	2026-11-02 20:08:43.131434	\N	0	010-1234-5678	김하늘	{bcrypt}$2a$10$xKDCiSXyutI437XlpXqPF.chRvsnI3nlfFdhBFgVc752rZBnn3Gaq	\N	\N	2026-11-02	\N	happybuyer
2	2026-11-02 20:08:43.218076	\N	2026-11-02 20:08:43.301094	\N	doyun@example.com	\N	\N	\N	f	2026-11-02 20:08:43.300931	\N	0	010-2345-6789	이도윤	{bcrypt}$2a$10$hpIf8myD8Ea7PN.pRE4kkudlEPuFCNbIeKMw1qYDOxDURb6tb/myK	\N	\N	2026-11-02	\N	retro_mania
3	2026-11-02 20:08:43.386368	\N	2026-11-02 20:08:43.470413	\N	seoyeon@example.com	\N	\N	\N	f	2026-11-02 20:08:43.470272	\N	0	010-3456-7890	박서연	{bcrypt}$2a$10$BIUdct99LAD0xomOBr7XcO.BLeCYxDQdxr0dEVLQ6if41j2puWY2.	\N	\N	2026-11-02	\N	camping_lee
4	2026-11-02 20:08:43.5621	\N	2026-11-02 20:08:43.648361	\N	minjun@example.com	\N	\N	\N	f	2026-11-02 20:08:43.648108	\N	0	010-4567-8901	최민준	{bcrypt}$2a$10$A40WFREAcCYXkzFQcAfMTOePdvpncSI6t01xP7CCSHyfwJAAs4gSi	\N	\N	2026-11-02	\N	gadget_kim
\.


--
-- Data for Name: member_address; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.member_address (id, address, detail_address, member_id, postal_code) FROM stdin;
1	서울특별시 마포구 월드컵북로 400	101동 1203호	1	00000
2	부산광역시 부산진구 중앙대로 672	3층	2	00000
3	대전광역시 서구 둔산로 100	502호	3	00000
4	경기도 성남시 분당구 판교역로 235	A동 7층	4	00000
\.


--
-- Data for Name: member_agreement; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.member_agreement (id, is_mail_reception_agreed, is_marketing_usage_agreed, is_sms_reception_agreed, member_id) FROM stdin;
1	t	t	t	1
2	t	f	f	2
3	t	f	t	3
4	t	t	t	4
\.


--
-- Data for Name: member_organization; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.member_organization (id, member_id, office_address, office_department, office_detail_address, office_name, office_position) FROM stdin;
1	1	\N	\N	\N	\N	\N
2	2	\N	\N	\N	\N	\N
3	3	\N	\N	\N	\N	\N
4	4	\N	\N	\N	\N	\N
\.


--
-- Data for Name: menu; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.menu (dtype, id, created_at, deleted_at, modified_at, depth, feature, is_using, link, name, parent_id, sequence, target_id, type, menu_footer, menu_header, menu_template) FROM stdin;
Menu	12	2026-11-02 11:25:24.66389	\N	2026-11-02 11:25:24.66389	1	\N	t	\N	사진 출처	9	2	5	CONTENT	\N	\N	\N
Menu	10	2026-11-02 20:08:42.832896	\N	2026-11-02 20:39:36.714005	1	\N	t	\N	안전거래 가이드	9	0	3	CONTENT	\N	\N	\N
Menu	11	2026-11-02 11:25:24.66389	\N	2026-11-02 20:39:36.714311	1	\N	t	\N	판매 수수료 안내	9	1	4	CONTENT	\N	\N	\N
Menu	4	2026-11-02 20:08:41.124635	\N	2026-11-02 20:41:39.107912	0	PRODUCT	t	\N	상품	\N	3	\N	FEATURE	\N	\N	\N
Menu	5	2026-11-02 20:08:41.1261	\N	2026-11-02 20:41:40.752598	0	PROGRAM	t	\N	프로그램	\N	4	\N	FEATURE	\N	\N	\N
Menu	6	2026-11-02 20:08:41.127713	\N	2026-11-02 20:41:41.979967	0	QNA	t	\N	질문답변	\N	5	\N	FEATURE	\N	\N	\N
Menu	7	2026-11-02 20:08:41.128969	\N	2026-11-02 20:41:43.039746	0	REPORT	t	\N	신고	\N	6	\N	FEATURE	\N	\N	\N
Menu	8	2026-11-02 20:08:41.130141	\N	2026-11-02 20:41:43.039942	0	MAP	t	\N	지도	\N	7	\N	FEATURE	\N	\N	\N
Menu	2	2026-11-02 20:08:41.121237	\N	2026-11-02 20:44:27.494966	0	BOARD	t	\N	커뮤니티	\N	1	\N	FEATURE	\N	\N	\N
Menu	3	2026-11-02 20:08:41.123091	\N	2026-11-02 20:44:34.846469	0	ARCHIVE	t	\N	공지사항	\N	2	\N	FEATURE	\N	\N	\N
Menu	9	2026-11-02 20:08:42.823778	\N	2026-11-02 20:45:18.667223	0	\N	t	\N	소개	\N	0	2	CONTENT	\N	\N	\N
\.


--
-- Data for Name: order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public."order" (id, created_at, deleted_at, modified_at, address, can_deliver, detail_address, discount_id, fields, order_number, order_sequence, phone_number, postal_code, product_id, product_name, product_option_id, quantity, receiver_name, status, total_price, user_id, user_type) FROM stdin;
3	2026-11-02 20:08:44.683696	\N	2026-11-02 20:08:44.691414	부산광역시 부산진구 중앙대로 672	t		\N	\N	O20261102200844-7249	\N	010-2345-6789	00000	3	닌텐도 스위치 네온 블루/레드	\N	1	이도윤	PAID	230000.00	2	MEMBER
4	2026-11-02 20:08:44.697696	\N	2026-11-02 20:08:44.697696	경기도 성남시 분당구 판교역로 235	t		\N	\N	O20261102200844-5308	\N	010-4567-8901	00000	4	다이슨 V10 앱솔루트 무선청소기	\N	1	최민준	PENDING_PAYMENT	280000.00	4	MEMBER
5	2026-11-02 20:08:44.707222	\N	2026-11-02 20:08:44.714529	경기도 성남시 분당구 판교역로 235	t		1	\N	O20261102200844-9659	\N	010-4567-8901	00000	2	맥북 에어 M2 13인치 스타라이트 16GB/512GB	\N	1	최민준	PAID	945000.00	4	MEMBER
1	2026-11-02 20:08:44.634671	\N	2026-11-02 20:08:44.732853	서울특별시 마포구 월드컵북로 400	t		1	\N	O20261102200844-9628	\N	010-1234-5678	00000	1	아이폰 15 Pro 256GB 내추럴 티타늄 (A급)	1	1	김하늘	DELIVERED	882000.00	1	MEMBER
2	2026-11-02 20:08:44.667305	\N	2026-11-02 20:08:44.7425	대전광역시 서구 둔산로 100	t		\N	\N	O20261102200844-2544	\N	010-3456-7890	00000	5	헬리녹스 그라운드 체어 2개 세트	\N	2	박서연	SHIPPING	240000.00	3	MEMBER
\.


--
-- Data for Name: payment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.payment (id, created_at, deleted_at, modified_at, amount, approved_at, canceled_at, member_id, method, status, target_id, target_type, transaction_id) FROM stdin;
1	2026-11-02 20:08:44.651736	\N	2026-11-02 20:08:44.651736	882000.00	2026-11-02 20:08:44.651524	\N	1	CARD	APPROVED	1	ORDER	MOCK-66b0277c-50f9-4a6f-bce4-beff2c8c5f57
2	2026-11-02 20:08:44.67528	\N	2026-11-02 20:08:44.67528	240000.00	2026-11-02 20:08:44.675211	\N	3	CARD	APPROVED	2	ORDER	MOCK-0eb9219c-a061-4e55-acf6-15b3cf3720ee
3	2026-11-02 20:08:44.690555	\N	2026-11-02 20:08:44.690555	230000.00	2026-11-02 20:08:44.690497	\N	2	CARD	APPROVED	3	ORDER	MOCK-cd5395f7-1b3a-4a0c-a605-7707166568dd
4	2026-11-02 20:08:44.713722	\N	2026-11-02 20:08:44.713722	945000.00	2026-11-02 20:08:44.713664	\N	4	CARD	APPROVED	5	ORDER	MOCK-00c52fae-c999-4644-b125-5b9ff50328ab
5	2026-11-02 20:08:44.942445	\N	2026-11-02 20:08:44.942445	20000.00	2026-11-02 20:08:44.942368	\N	2	BANK_TRANSFER	APPROVED	1	RESERVATION	MOCK-a2e850e5-19b3-4fed-a38c-5ae1b808fd44
6	2026-11-02 20:08:44.961065	\N	2026-11-02 20:08:44.961065	15000.00	2026-11-02 20:08:44.960992	\N	4	BANK_TRANSFER	APPROVED	2	RESERVATION	MOCK-d210b1c8-ee21-4cb7-b251-0e05ca5f89d2
7	2026-11-02 20:08:44.976726	\N	2026-11-02 20:08:44.976726	30000.00	2026-11-02 20:08:44.976678	\N	1	BANK_TRANSFER	APPROVED	3	RESERVATION	MOCK-73130c50-baf1-478c-be18-c64211fc24cf
\.


--
-- Data for Name: policy; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.policy (id, created_at, deleted_at, modified_at, content_html, last_worker, type, version) FROM stdin;
1	2026-11-02 20:08:42.617357	\N	2026-11-02 20:08:42.619887	<h3>개인정보 수집·이용 동의</h3><ul><li>수집 항목: 아이디, 비밀번호, 이름, 이메일, 휴대전화 번호, 배송지 주소</li><li>이용 목적: 회원 식별, 중고 상품 주문·배송, 안전거래 분쟁 처리, 고객 문의 응대</li><li>보유 기간: 회원 탈퇴 시까지 (관계 법령에 따라 거래 기록은 5년 보관)</li></ul><p>동의를 거부할 수 있으나, 거부하면 회원가입과 주문을 할 수 없습니다.</p>	admin	PRIVACY_CONSENT	1
2	2026-11-02 20:08:42.632981	\N	2026-11-02 20:08:42.633831	<h3>중고천국 개인정보처리방침</h3><p>중고천국(이하 &#34;회사&#34;)은 이용자의 개인정보를 소중히 여기며 「개인정보 보호법」을 준수합니다.</p><h4>1. 처리하는 개인정보 항목</h4><p>아이디, 이름, 이메일, 휴대전화 번호, 배송지 주소, 결제 기록</p><h4>2. 개인정보의 제3자 제공</h4><p>배송을 위해 택배사에 수령인 이름·연락처·주소를 제공합니다.</p><h4>3. 개인정보 보호책임자</h4><p>개인정보 보호책임자: 운영팀장 / privacy&#64;junggo-heaven.example</p>	admin	PRIVACY_POLICY	1
\.


--
-- Data for Name: product; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.product (id, created_at, deleted_at, modified_at, available_quantity, content_html, content_text, description, fields, image_file_id, is_enabled, name, price) FROM stdin;
6	2026-11-02 20:08:44.411666	\N	2026-11-02 20:08:44.411666	1	<h3>검수 리포트</h3><ul><li>키보드 각인 깨끗</li><li>화면 번인 없음</li></ul><p>중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.</p>	검수 리포트 키보드 각인 깨끗 화면 번인 없음 중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.	키보드 각인 깨끗, 화면 번인 없음	\N	6	t	LG 그램 16 (2023) i7/16GB	890000.00
7	2026-11-02 20:08:44.455187	\N	2026-11-02 20:08:44.455187	6	<h3>검수 리포트</h3><ul><li>이어패드 새 제품으로 교체</li></ul><p>중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.</p>	검수 리포트 이어패드 새 제품으로 교체 중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.	이어패드 새 제품으로 교체	\N	7	t	소니 WH-1000XM3 블랙	150000.00
8	2026-11-02 20:08:44.504536	\N	2026-11-02 20:08:44.504536	2	<h3>검수 리포트</h3><ul><li>성수 픽업라운지 직접 수령</li></ul><p>중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.</p>	검수 리포트 성수 픽업라운지 직접 수령 중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.	성수 픽업라운지 직접 수령	\N	8	t	이케아 포엥 암체어 (쿠션 포함)	70000.00
9	2026-11-02 20:08:44.555237	\N	2026-11-02 20:08:44.555237	0	<p>판매가 중지된 상품입니다.</p>	판매가 중지된 상품입니다.	판매자 요청으로 판매 중지	\N	9	f	갤럭시 Z 플립4 (판매 중지)	350000.00
1	2026-11-02 20:08:44.172075	\N	2026-11-02 20:08:44.637917	2	<h3>검수 리포트</h3><ul><li>배터리 성능 94%</li><li>액정 무결점</li></ul><p>중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.</p>	검수 리포트 배터리 성능 94% 액정 무결점 중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.	배터리 성능 94%, 액정 무결점	\N	1	t	아이폰 15 Pro 256GB 내추럴 티타늄	980000.00
5	2026-11-02 20:08:44.361307	\N	2026-11-02 20:08:44.668717	2	<h3>검수 리포트</h3><ul><li>캠핑 4회 사용</li><li>수납 파우치 포함</li></ul><p>중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.</p>	검수 리포트 캠핑 4회 사용 수납 파우치 포함 중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.	캠핑 4회 사용, 수납 파우치 포함	\N	5	t	헬리녹스 그라운드 체어 2개 세트	120000.00
3	2026-11-02 20:08:44.26652	\N	2026-11-02 20:08:44.684853	4	<h3>검수 리포트</h3><ul><li>조이콘 쏠림 없음</li><li>독 포함 박스 풀구성</li></ul><p>중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.</p>	검수 리포트 조이콘 쏠림 없음 독 포함 박스 풀구성 중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.	조이콘 쏠림 없음, 독 포함 박스 풀구성	\N	3	t	닌텐도 스위치 네온 블루/레드	230000.00
4	2026-11-02 20:08:44.308893	\N	2026-11-02 20:08:44.698648	1	<h3>검수 리포트</h3><ul><li>배터리 교체 완료</li><li>헤드 3종</li></ul><p>중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.</p>	검수 리포트 배터리 교체 완료 헤드 3종 중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.	배터리 교체 완료, 헤드 3종	\N	4	t	다이슨 V10 앱솔루트 무선청소기	280000.00
2	2026-11-02 20:08:44.230466	\N	2026-11-02 20:08:44.708302	1	<h3>검수 리포트</h3><ul><li>배터리 사이클 87회</li><li>정품 충전기 포함</li></ul><p>중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.</p>	검수 리포트 배터리 사이클 87회 정품 충전기 포함 중고천국 검수센터에서 기능과 외관을 직접 점검한 상품입니다. 수령 후 7일 안에 검수 내용과 다르면 환불됩니다.	배터리 사이클 87회, 정품 충전기 포함	\N	2	t	맥북 에어 M2 13인치 스타라이트 16GB/512GB	1050000.00
\.


--
-- Data for Name: product_option; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.product_option (id, created_at, deleted_at, modified_at, additional_price, description, fields, name, product_id) FROM stdin;
1	2026-11-02 20:08:44.177518	\N	2026-11-02 20:08:44.177518	0.00	배터리 90% 이상	\N	A급	1
2	2026-11-02 20:08:44.179642	\N	2026-11-02 20:08:44.179642	150000.00	미개봉 리퍼	\N	S급	1
3	2026-11-02 20:08:44.232564	\N	2026-11-02 20:08:44.232564	0.00	상판 생활 흠집	\N	B급	2
4	2026-11-02 20:08:44.233502	\N	2026-11-02 20:08:44.233502	100000.00	외관 미세 사용감	\N	A급	2
5	2026-11-02 20:08:44.311508	\N	2026-11-02 20:08:44.311508	0.00	사용감 거의 없음	\N	A급	4
\.


--
-- Data for Name: program; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.program (id, created_at, deleted_at, modified_at, application_end_date_time, application_start_date_time, available_person, content_html, content_text, description, end_date_time, fields, image_file_id, is_enabled, maximum_person, name, price, start_date_time) FROM stdin;
3	2026-11-02 20:08:44.915865	\N	2026-11-02 20:08:44.915865	2026-11-20 18:00:00	2026-10-26 09:00:00	8	<p>재료비가 포함되어 있습니다. 리폼할 의자를 가져오시면 됩니다.</p>	재료비가 포함되어 있습니다. 리폼할 의자를 가져오시면 됩니다.	낡은 캠핑 의자를 새 원단으로 리폼	2026-11-23 17:00:00	\N	12	t	8	캠핑용품 업사이클링 워크숍 (대전)	35000.00	2026-11-22 11:00:00
2	2026-11-02 20:08:44.875242	\N	2026-11-02 20:08:44.97081	2026-11-09 18:00:00	2026-10-26 09:00:00	9	<p>배터리·디스플레이·키보드·포트 점검을 실습합니다.</p><p>개인 노트북을 지참하면 1:1로 점검해 드립니다.</p>	배터리·디스플레이·키보드·포트 점검을 실습합니다. 개인 노트북을 지참하면 1:1로 점검해 드립니다.	검수사에게 배우는 중고 노트북 점검법 (2시간)	2026-11-12 17:00:00	\N	11	t	12	중고 노트북 셀프 점검 클래스	15000.00	2026-11-11 11:00:00
1	2026-11-02 20:08:44.817219	\N	2026-11-02 20:08:44.986728	2026-11-12 18:00:00	2026-10-26 09:00:00	28	<p>부스 1칸(2m×2m)과 테이블 1개를 제공합니다.</p><p>당일 판매 수수료는 없습니다.</p>	부스 1칸(2m×2m)과 테이블 1개를 제공합니다. 당일 판매 수수료는 없습니다.	성수 픽업라운지 앞마당에서 열리는 주말 플리마켓 판매 부스	2026-11-16 17:00:00	\N	10	t	30	주말 플리마켓 셀러 모집 (성수)	20000.00	2026-11-14 11:00:00
\.


--
-- Data for Name: question; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.question (id, created_at, deleted_at, modified_at, author_name, content_html, content_text, email, fields, hit, ip_address, is_secret, likes, password, title, user_id, user_type) FROM stdin;
1	2026-11-02 20:08:45.008317	\N	2026-11-02 20:08:45.008317	김하늘	<p>판매하려고 하는데 검수센터 도착 후 며칠이면 판매 등록되나요?</p>	판매하려고 하는데 검수센터 도착 후 며칠이면 판매 등록되나요?	\N	\N	0	0:0:0:0:0:0:0:1	f	0	\N	검수 기간은 얼마나 걸리나요?	1	MEMBER
2	2026-11-02 20:08:45.032927	\N	2026-11-02 20:08:45.032927	최민준	<p>맥북 키 하나가 살짝 뻑뻑합니다. 환불 가능할까요?</p>	맥북 키 하나가 살짝 뻑뻑합니다. 환불 가능할까요?	\N	\N	0	0:0:0:0:0:0:0:1	t	0	\N	수령 후 하자를 발견했어요	4	MEMBER
3	2026-11-02 20:08:45.126946	\N	2026-11-02 20:08:45.126946	궁금이	<p>가입 없이 한 번만 사보고 싶어요.</p>	가입 없이 한 번만 사보고 싶어요.	guest@example.com	\N	0	0:0:0:0:0:0:0:1	f	0	{bcrypt}$2a$10$bUnNEnaYukekOJwppJUhv.vEnDXKBFc3pDl9RIS5PkXqy3mUUNy0y	비회원도 구매할 수 있나요?	\N	GUEST
\.


--
-- Data for Name: report; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.report (id, created_at, deleted_at, modified_at, content_html, content_text, email, fields, password, phone_number, reporter_name, status, title, user_id) FROM stdin;
1	2026-11-02 20:08:45.138815	\N	2026-11-02 20:08:45.166552	<p>자유게시판에서 연락 온 사람이 카카오톡으로 옮겨 계좌이체를 요구했습니다.</p>	자유게시판에서 연락 온 사람이 카카오톡으로 옮겨 계좌이체를 요구했습니다.	doyun@example.com	\N	\N	010-2345-6789	이도윤	ANSWERED	외부 메신저로 선입금 요구	2
2	2026-11-02 20:08:45.256479	\N	2026-11-02 20:08:45.256479	<p>자유게시판에 올라온 명품 지갑 사진이 가품 같습니다.</p>	자유게시판에 올라온 명품 지갑 사진이 가품 같습니다.	\N	\N	{bcrypt}$2a$10$m9FT6lG5GfDmY8/nHjwAQOkqTfyb/VHK8DzklQiXtu4dtpksnrZZC	\N	익명	RECEIVED	가품 의심 상품	\N
\.


--
-- Data for Name: report_answer; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.report_answer (id, created_at, deleted_at, modified_at, author_name, content_html, content_text, depth, "order", report_ref_id, status, title) FROM stdin;
1	2026-11-02 20:08:45.164387	\N	2026-11-02 20:08:45.164387	관리자	<p>해당 계정을 이용 정지하고 수사기관 협조 요청을 진행 중입니다. 신고해 주셔서 감사합니다.</p>	해당 계정을 이용 정지하고 수사기관 협조 요청을 진행 중입니다. 신고해 주셔서 감사합니다.	0	0	1	APPROVED	신고 접수 및 조치 안내
\.


--
-- Data for Name: reservation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.reservation (id, created_at, deleted_at, modified_at, applicant_name, discount_id, fields, phone_number, product_id, program_name, quantity, reservation_number, reservation_sequence, status, total_price, user_id, user_type) FROM stdin;
1	2026-11-02 20:08:44.930772	\N	2026-11-02 20:08:44.943822	이도윤	\N	\N	010-2345-6789	1	주말 플리마켓 셀러 모집 (성수)	1	R20261102200844-6465	\N	CONFIRMED	20000.00	2	MEMBER
2	2026-11-02 20:08:44.953542	\N	2026-11-02 20:08:44.962272	최민준	\N	\N	010-4567-8901	2	중고 노트북 셀프 점검 클래스	1	R20261102200844-6603	\N	CONFIRMED	15000.00	4	MEMBER
3	2026-11-02 20:08:44.969633	\N	2026-11-02 20:08:44.977954	김하늘	\N	\N	010-1234-5678	2	중고 노트북 셀프 점검 클래스	2	R20261102200844-9755	\N	CONFIRMED	30000.00	1	MEMBER
4	2026-11-02 20:08:44.98568	\N	2026-11-02 20:08:44.98568	박서연	\N	\N	010-3456-7890	1	주말 플리마켓 셀러 모집 (성수)	1	R20261102200844-2971	\N	PENDING_PAYMENT	20000.00	3	MEMBER
\.


--
-- Data for Name: review; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.review (id, created_at, deleted_at, modified_at, author_name, content_html, content_text, fields, hit, ip_address, is_secret, likes, password, product_id, program_id, rating, target_type, title, user_id, user_type) FROM stdin;
1	2026-11-02 20:08:44.759305	\N	2026-11-02 20:08:44.759305	김하늘	<p>배터리 94% 맞고 외관도 깨끗해요. 다음에도 여기서 살게요.</p>	배터리 94% 맞고 외관도 깨끗해요. 다음에도 여기서 살게요.	\N	0	0:0:0:0:0:0:0:1	f	0	\N	1	\N	5	PRODUCT	검수 리포트 그대로입니다	1	MEMBER
2	2026-11-02 20:08:44.769586	\N	2026-11-02 20:08:44.769586	박서연	<p>파우치에 작은 얼룩이 있었지만 의자는 새것 같아요.</p>	파우치에 작은 얼룩이 있었지만 의자는 새것 같아요.	\N	0	0:0:0:0:0:0:0:1	f	0	\N	5	\N	4	PRODUCT	가볍고 튼튼해요	3	MEMBER
3	2026-11-02 20:08:44.777341	\N	2026-11-02 20:08:44.777341	이도윤	<p>특가로 저렴하게 샀습니다. 조이콘 상태 최고!</p>	특가로 저렴하게 샀습니다. 조이콘 상태 최고!	\N	0	0:0:0:0:0:0:0:1	f	0	\N	3	\N	5	PRODUCT	아이랑 잘 쓰고 있어요	2	MEMBER
4	2026-11-02 20:08:44.997805	\N	2026-11-02 20:08:44.997805	최민준	<p>사전 안내 메일이 친절했어요. 노트북 챙겨 갑니다!</p>	사전 안내 메일이 친절했어요. 노트북 챙겨 갑니다!	\N	0	0:0:0:0:0:0:0:1	f	0	\N	\N	2	5	PROGRAM	신청 전부터 기대돼요	4	MEMBER
\.


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.role (id, created_at, deleted_at, modified_at, code, description, is_admin, is_default, name) FROM stdin;
\.


--
-- Data for Name: satisfaction; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.satisfaction (id, created_at, deleted_at, modified_at, five_point, four_point, menu_id, one_point, three_point, two_point, user_id) FROM stdin;
1	2026-11-02 20:08:45.272189	\N	2026-11-02 20:08:45.30222	2	1	9	0	0	0	\N
\.


--
-- Data for Name: satisfaction_vote; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.satisfaction_vote (id, created_at, deleted_at, modified_at, member_id, menu_id, opinion, score) FROM stdin;
1	2026-11-02 20:08:45.274509	\N	2026-11-02 20:08:45.274509	1	9	안전거래 설명이 이해하기 쉬웠어요	5
2	2026-11-02 20:08:45.285852	\N	2026-11-02 20:08:45.285852	4	9	체크리스트를 인쇄용으로도 주세요	4
3	2026-11-02 20:08:45.301175	\N	2026-11-02 20:08:45.301175	3	9	\N	5
\.


--
-- Data for Name: site; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.site (id, created_at, deleted_at, modified_at, description, domain, home_content_id, is_default, locale, name, theme, type) FROM stdin;
1	2026-11-02 20:08:42.565569	\N	2026-11-02 20:08:42.742477	검수부터 안전결제까지, 믿고 사고파는 중고거래 플랫폼	www.junggo-heaven.example	1	t	ko	중고천국	market	\N
\.


--
-- Data for Name: site_disabled_feature; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.site_disabled_feature (site_id, feature) FROM stdin;
\.


--
-- Data for Name: statistics; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.statistics (id, created_at, deleted_at, modified_at, browser, count, date, nation_code, os, url) FROM stdin;
\.


--
-- Data for Name: statistics_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.statistics_log (id, created_at, deleted_at, modified_at, ip_address, member_username, referer, request_method, url) FROM stdin;
\.


--
-- Data for Name: watchdog_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.watchdog_log (id, created_at, deleted_at, modified_at, content, ip_address, member_username, method_name, uri) FROM stdin;
\.


--
-- Name: admin_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.admin_address_id_seq', 1, false);


--
-- Name: admin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.admin_id_seq', 1, true);


--
-- Name: admin_organization_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.admin_organization_id_seq', 1, false);


--
-- Name: answer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.answer_id_seq', 2, true);


--
-- Name: archive_article_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.archive_article_file_id_seq', 1, false);


--
-- Name: archive_article_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.archive_article_id_seq', 8, true);


--
-- Name: archive_comment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.archive_comment_id_seq', 1, false);


--
-- Name: archive_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.archive_id_seq', 2, true);


--
-- Name: banner_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.banner_id_seq', 4, true);


--
-- Name: board_article_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_article_file_id_seq', 1, false);


--
-- Name: board_article_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_article_id_seq', 6, true);


--
-- Name: board_article_like_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_article_like_id_seq', 2, true);


--
-- Name: board_comment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_comment_id_seq', 2, true);


--
-- Name: board_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_config_id_seq', 2, true);


--
-- Name: board_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.board_id_seq', 2, true);


--
-- Name: branch_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.branch_id_seq', 4, true);


--
-- Name: capability_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.capability_id_seq', 1, false);


--
-- Name: code_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.code_id_seq', 9, true);


--
-- Name: config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.config_id_seq', 2, true);


--
-- Name: content_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.content_id_seq', 6, true);


--
-- Name: discount_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.discount_id_seq', 3, true);


--
-- Name: file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.file_id_seq', 13, true);


--
-- Name: group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.group_id_seq', 1, false);


--
-- Name: guest_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.guest_id_seq', 1, false);


--
-- Name: map_setting_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.map_setting_id_seq', 1, true);


--
-- Name: member_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.member_address_id_seq', 4, true);


--
-- Name: member_agreement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.member_agreement_id_seq', 4, true);


--
-- Name: member_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.member_id_seq', 4, true);


--
-- Name: member_organization_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.member_organization_id_seq', 4, true);


--
-- Name: menu_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.menu_id_seq', 12, true);


--
-- Name: order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.order_id_seq', 5, true);


--
-- Name: payment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.payment_id_seq', 7, true);


--
-- Name: policy_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.policy_id_seq', 2, true);


--
-- Name: product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.product_id_seq', 9, true);


--
-- Name: product_option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.product_option_id_seq', 5, true);


--
-- Name: program_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.program_id_seq', 3, true);


--
-- Name: question_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.question_id_seq', 3, true);


--
-- Name: report_answer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.report_answer_id_seq', 1, true);


--
-- Name: report_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.report_id_seq', 2, true);


--
-- Name: reservation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.reservation_id_seq', 4, true);


--
-- Name: review_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.review_id_seq', 4, true);


--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_id_seq', 1, false);


--
-- Name: satisfaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.satisfaction_id_seq', 1, true);


--
-- Name: satisfaction_vote_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.satisfaction_vote_id_seq', 3, true);


--
-- Name: site_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.site_id_seq', 1, true);


--
-- Name: statistics_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.statistics_id_seq', 1, false);


--
-- Name: statistics_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.statistics_log_id_seq', 1, false);


--
-- Name: watchdog_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.watchdog_log_id_seq', 1, false);


--
-- Name: admin_address admin_address_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin_address
    ADD CONSTRAINT admin_address_pkey PRIMARY KEY (id);


--
-- Name: admin_organization admin_organization_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin_organization
    ADD CONSTRAINT admin_organization_pkey PRIMARY KEY (id);


--
-- Name: admin admin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin
    ADD CONSTRAINT admin_pkey PRIMARY KEY (id);


--
-- Name: answer answer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.answer
    ADD CONSTRAINT answer_pkey PRIMARY KEY (id);


--
-- Name: archive_article_file archive_article_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.archive_article_file
    ADD CONSTRAINT archive_article_file_pkey PRIMARY KEY (id);


--
-- Name: archive_article archive_article_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.archive_article
    ADD CONSTRAINT archive_article_pkey PRIMARY KEY (id);


--
-- Name: archive_comment archive_comment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.archive_comment
    ADD CONSTRAINT archive_comment_pkey PRIMARY KEY (id);


--
-- Name: archive archive_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.archive
    ADD CONSTRAINT archive_pkey PRIMARY KEY (id);


--
-- Name: authenticated_guest authenticated_guest_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authenticated_guest
    ADD CONSTRAINT authenticated_guest_pkey PRIMARY KEY (id);


--
-- Name: authenticated_member authenticated_member_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authenticated_member
    ADD CONSTRAINT authenticated_member_pkey PRIMARY KEY (id);


--
-- Name: banner banner_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.banner
    ADD CONSTRAINT banner_pkey PRIMARY KEY (id);


--
-- Name: board_article_file board_article_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_article_file
    ADD CONSTRAINT board_article_file_pkey PRIMARY KEY (id);


--
-- Name: board_article_like board_article_like_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_article_like
    ADD CONSTRAINT board_article_like_pkey PRIMARY KEY (id);


--
-- Name: board_article board_article_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_article
    ADD CONSTRAINT board_article_pkey PRIMARY KEY (id);


--
-- Name: board_comment board_comment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_comment
    ADD CONSTRAINT board_comment_pkey PRIMARY KEY (id);


--
-- Name: board_config board_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_config
    ADD CONSTRAINT board_config_pkey PRIMARY KEY (id);


--
-- Name: board board_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board
    ADD CONSTRAINT board_pkey PRIMARY KEY (id);


--
-- Name: branch branch_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.branch
    ADD CONSTRAINT branch_pkey PRIMARY KEY (id);


--
-- Name: capability capability_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.capability
    ADD CONSTRAINT capability_pkey PRIMARY KEY (id);


--
-- Name: code code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code
    ADD CONSTRAINT code_pkey PRIMARY KEY (id);


--
-- Name: config config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.config
    ADD CONSTRAINT config_pkey PRIMARY KEY (id);


--
-- Name: content content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content
    ADD CONSTRAINT content_pkey PRIMARY KEY (id);


--
-- Name: discount discount_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.discount
    ADD CONSTRAINT discount_pkey PRIMARY KEY (id);


--
-- Name: file file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file
    ADD CONSTRAINT file_pkey PRIMARY KEY (id);


--
-- Name: group group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."group"
    ADD CONSTRAINT group_pkey PRIMARY KEY (id);


--
-- Name: guest guest_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.guest
    ADD CONSTRAINT guest_pkey PRIMARY KEY (id);


--
-- Name: image_file image_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.image_file
    ADD CONSTRAINT image_file_pkey PRIMARY KEY (id);


--
-- Name: map_setting map_setting_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.map_setting
    ADD CONSTRAINT map_setting_pkey PRIMARY KEY (id);


--
-- Name: member_address member_address_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.member_address
    ADD CONSTRAINT member_address_pkey PRIMARY KEY (id);


--
-- Name: member_agreement member_agreement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.member_agreement
    ADD CONSTRAINT member_agreement_pkey PRIMARY KEY (id);


--
-- Name: member_organization member_organization_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.member_organization
    ADD CONSTRAINT member_organization_pkey PRIMARY KEY (id);


--
-- Name: member member_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.member
    ADD CONSTRAINT member_pkey PRIMARY KEY (id);


--
-- Name: menu menu_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.menu
    ADD CONSTRAINT menu_pkey PRIMARY KEY (id);


--
-- Name: order order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."order"
    ADD CONSTRAINT order_pkey PRIMARY KEY (id);


--
-- Name: payment payment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (id);


--
-- Name: policy policy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.policy
    ADD CONSTRAINT policy_pkey PRIMARY KEY (id);


--
-- Name: product_option product_option_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_option
    ADD CONSTRAINT product_option_pkey PRIMARY KEY (id);


--
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- Name: program program_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.program
    ADD CONSTRAINT program_pkey PRIMARY KEY (id);


--
-- Name: question question_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question
    ADD CONSTRAINT question_pkey PRIMARY KEY (id);


--
-- Name: report_answer report_answer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_answer
    ADD CONSTRAINT report_answer_pkey PRIMARY KEY (id);


--
-- Name: report report_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report
    ADD CONSTRAINT report_pkey PRIMARY KEY (id);


--
-- Name: reservation reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_pkey PRIMARY KEY (id);


--
-- Name: review review_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.review
    ADD CONSTRAINT review_pkey PRIMARY KEY (id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: satisfaction satisfaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.satisfaction
    ADD CONSTRAINT satisfaction_pkey PRIMARY KEY (id);


--
-- Name: satisfaction_vote satisfaction_vote_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.satisfaction_vote
    ADD CONSTRAINT satisfaction_vote_pkey PRIMARY KEY (id);


--
-- Name: site site_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_pkey PRIMARY KEY (id);


--
-- Name: statistics_log statistics_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.statistics_log
    ADD CONSTRAINT statistics_log_pkey PRIMARY KEY (id);


--
-- Name: statistics statistics_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.statistics
    ADD CONSTRAINT statistics_pkey PRIMARY KEY (id);


--
-- Name: order uk4b9qgonn9wr1584ye6j5acwga; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."order"
    ADD CONSTRAINT uk4b9qgonn9wr1584ye6j5acwga UNIQUE (order_number);


--
-- Name: satisfaction uk6ns3nnmtklpfwt92lgul76ho1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.satisfaction
    ADD CONSTRAINT uk6ns3nnmtklpfwt92lgul76ho1 UNIQUE (menu_id);


--
-- Name: site_disabled_feature uk9m02n4voe4ubbjoj27hd6rcwv; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.site_disabled_feature
    ADD CONSTRAINT uk9m02n4voe4ubbjoj27hd6rcwv UNIQUE (site_id, feature);


--
-- Name: board_article_like uk_board_article_like; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_article_like
    ADD CONSTRAINT uk_board_article_like UNIQUE (article_id, member_id);


--
-- Name: satisfaction_vote uk_satisfaction_vote; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.satisfaction_vote
    ADD CONSTRAINT uk_satisfaction_vote UNIQUE (menu_id, member_id);


--
-- Name: board_config ukb1naws1dbvqj6gkho2eeq2p6u; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board_config
    ADD CONSTRAINT ukb1naws1dbvqj6gkho2eeq2p6u UNIQUE (board_id);


--
-- Name: answer ukcx17lpi7ukqke10sganse4y4h; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.answer
    ADD CONSTRAINT ukcx17lpi7ukqke10sganse4y4h UNIQUE (question_ref_id);


--
-- Name: board ukfipjuh5hisfppc2n34giej12x; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.board
    ADD CONSTRAINT ukfipjuh5hisfppc2n34giej12x UNIQUE (name);


--
-- Name: policy ukin7upe574rh90pkiuu0clwr3u; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.policy
    ADD CONSTRAINT ukin7upe574rh90pkiuu0clwr3u UNIQUE (type);


--
-- Name: archive ukpnk12l4936a21pst3p696mgt6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.archive
    ADD CONSTRAINT ukpnk12l4936a21pst3p696mgt6 UNIQUE (name);


--
-- Name: reservation ukroa20xex1gu08f092ru2mb0s7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT ukroa20xex1gu08f092ru2mb0s7 UNIQUE (reservation_number);


--
-- Name: payment uktacis04bqalsngo46yvxlo7yb; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT uktacis04bqalsngo46yvxlo7yb UNIQUE (transaction_id);


--
-- Name: watchdog_log watchdog_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.watchdog_log
    ADD CONSTRAINT watchdog_log_pkey PRIMARY KEY (id);


--
-- Name: idx_archive_article_file_article; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_archive_article_file_article ON public.archive_article_file USING btree (article_id);


--
-- Name: idx_archive_comment_article; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_archive_comment_article ON public.archive_comment USING btree (article_id);


--
-- Name: idx_board_article_file_article; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_board_article_file_article ON public.board_article_file USING btree (article_id);


--
-- Name: idx_board_comment_article; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_board_comment_article ON public.board_comment USING btree (article_id);


--
-- Name: idx_payment_target; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_target ON public.payment USING btree (target_type, target_id);


--
-- Name: idx_report_answer_report; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_report_answer_report ON public.report_answer USING btree (report_ref_id);


--
-- Name: idx_review_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_product ON public.review USING btree (product_id);


--
-- Name: idx_review_program; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_program ON public.review USING btree (program_id);


--
-- Name: idx_statistics_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_statistics_date ON public.statistics USING btree (date);


--
-- Name: site_disabled_feature fk1kpb206slo8p74wua2jt8acq2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.site_disabled_feature
    ADD CONSTRAINT fk1kpb206slo8p74wua2jt8acq2 FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- Name: authenticated_guest fkclaw43pl3ywj9xqnj1tb80vfk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authenticated_guest
    ADD CONSTRAINT fkclaw43pl3ywj9xqnj1tb80vfk FOREIGN KEY (id) REFERENCES public.guest(id);


--
-- Name: image_file fki7u9ksavdayjkjfii5rpehtl6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.image_file
    ADD CONSTRAINT fki7u9ksavdayjkjfii5rpehtl6 FOREIGN KEY (id) REFERENCES public.file(id);


--
-- Name: authenticated_member fkq4wd2pcemgytw8dtjexik7ffc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authenticated_member
    ADD CONSTRAINT fkq4wd2pcemgytw8dtjexik7ffc FOREIGN KEY (id) REFERENCES public.member(id);


--
-- PostgreSQL database dump complete
--

\unrestrict LX6nUNWpJfiMLX8trQYu4rDaAwDpc68PI33Lrcgz2gyUnFewly6Ot6wRnXrrsYv


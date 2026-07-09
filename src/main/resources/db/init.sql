-- ENUM TYPES

CREATE TYPE company_status AS ENUM (
    'PENDING', -- Chưa kích hoạt
    'ACTIVE', -- Đang hoạt động
    'LOCKED' -- Đã bị xóa
    );

CREATE TYPE branch_status AS ENUM (
    'ACTIVE', -- Chi nhánh đang hoạt động
    'PENDING', -- Chi nhánh tạm ngưng hoạt động
    'CLOSED' -- Đóng hoàn toàn
    );

CREATE TYPE employee_status AS ENUM (
    'PROBATION', -- Đang thử việc
    'ACTIVE', -- Đang làm việc chính thức
    'ON_LEAVE', -- Đang nghỉ phép
    'RESIGNED', -- Đã nghỉ việc (tự nghỉ)
    'TERMINATED' -- Bị sa thải
    );

CREATE TYPE gender AS ENUM (
    'MALE',
    'FEMALE',
    'OTHER'
    );

CREATE TYPE attendance_status AS ENUM (
    'PRESENT', -- Có mặt
    'ABSENT', -- Vắng mặt
    'LATE', -- Đi trễ
    'HALF_DAY', -- Nửa ngày
    'REMOTE', -- Làm việc từ xa
    'LEAVE' -- Nghỉ phép có phép
    );

CREATE TYPE checked_status AS ENUM (
    'CHECKED_IN',
    'CHECKED_OUT',
    'NOT_CHECKED'
    );

CREATE TYPE column_category AS ENUM (
    'TO_DO',
    'IN_PROGRESS',
    'DONE'
    );

CREATE TYPE project_invitation_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'EXPIRED',
    'REVOKED'
    );

CREATE TYPE project_member_status AS ENUM (
    'ACTIVE',
    'PENDING_APPROVAL',
    'INACTIVE'
    );

CREATE TYPE project_status AS ENUM (
    'ACTIVE',
    'COMPLETED',
    'ARCHIVED'
    );

CREATE TYPE sprint_status AS ENUM (
    'PLANNED',
    'ACTIVE',
    'COMPLETED'
    );

CREATE TYPE task_activity_action AS ENUM (
    'CREATE_TASK',
    'UPDATE_STATUS',
    'CHANGE_ASSIGNEE',
    'UPDATE_PRIORITY',
    'CHANGE_SPRINT',
    'UPDATE_POINTS',
    'ADD_COMMENT',
    'ADD_ATTACHMENT'
    );

CREATE TYPE task_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'URGENT'
    );

CREATE TYPE task_type AS ENUM (
    'STORY',
    'TASK',
    'BUG',
    'EPIC'
    );

CREATE TYPE kpi_status AS ENUM (
    'IN_PROGRESS',
    'ACHIEVED',
    'FAILED'
    );

CREATE TYPE performance_review_status AS ENUM (
    'DRAFT',
    'SELF_REVIEW',
    'MANAGER_REVIEW',
    'COMPLETED'
    );

CREATE TYPE leave_request_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED'
    );

CREATE TYPE leave_session_type AS ENUM (
    'FULL_DAY',
    'MORNING',
    'AFTERNOON'
    );

CREATE TYPE contract_status AS ENUM (
    'ACTIVE',
    'EXPIRED',
    'TERMINATED'
    );

CREATE TYPE payroll_period_status AS ENUM (
    'DRAFT',
    'PENDING_APPROVAL',
    'APPROVED',
    'PAID'
    );

CREATE TYPE payslip_status AS ENUM (
    'DRAFT',
    'SENT',
    'PAID'
    );

-- Table: companies

CREATE TABLE companies
(
    id                   BIGSERIAL PRIMARY KEY,
    company_code         VARCHAR(50)    NOT NULL,
    name                 VARCHAR(255)   NOT NULL,
    field                VARCHAR(255)   NOT NULL,
    website              VARCHAR(255) NULL,
    tax_code             VARCHAR(20)    NOT NULL,
    phone                VARCHAR(20)    NOT NULL,
    representative_email VARCHAR(255)   NOT NULL,
    status               company_status NOT NULL DEFAULT 'PENDING',
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX uq_companies_tax_code ON companies (tax_code);
CREATE UNIQUE INDEX uq_companies_rep_email ON companies (representative_email);
CREATE UNIQUE INDEX uq_companies_company_code ON companies (company_code);
CREATE INDEX idx_companies_status ON companies (status);

-- Table: branches

CREATE TABLE branches
(
    id                     BIGSERIAL PRIMARY KEY,
    company_id             BIGINT        NOT NULL,
    branch_code            VARCHAR(50)   NOT NULL,
    name                   VARCHAR(255)  NOT NULL,
    field                  VARCHAR(255)  NOT NULL,
    website                VARCHAR(255) NULL,
    address                TEXT NULL,
    status                 branch_status NOT NULL DEFAULT 'ACTIVE',
    standard_hours_per_day INTEGER       NOT NULL DEFAULT 8,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at             TIMESTAMPTZ NULL,
    CONSTRAINT fk_branches_companies FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE UNIQUE INDEX uq_branches_company_branch_code ON branches (company_id, branch_code);
CREATE INDEX idx_branches_company_id ON branches (company_id);
CREATE INDEX idx_branches_status ON branches (status);

-- Table: departments

CREATE TABLE departments
(
    id          BIGSERIAL PRIMARY KEY,
    branch_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL,
    CONSTRAINT fk_departments_branches FOREIGN KEY (branch_id) REFERENCES branches (id)
);

CREATE INDEX idx_departments_branch_id ON departments (branch_id);

-- Table: positions

CREATE TABLE positions
(
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL,
    CONSTRAINT fk_positions_companies FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE INDEX idx_positions_companies ON positions (company_id);


-- Table: employees

CREATE TABLE employees
(
    id            BIGSERIAL PRIMARY KEY,
    company_id    BIGINT          NOT NULL,
    branch_id     BIGINT          NOT NULL,
    department_id BIGINT          NOT NULL,
    position_id   BIGINT          NOT NULL,
    employee_code VARCHAR(50)     NOT NULL,
    full_name     VARCHAR(255)    NOT NULL,
    avatar_url    TEXT NULL,
    date_of_birth DATE NULL,
    gender        gender NULL,
    email         VARCHAR(255) NULL,
    phone         VARCHAR(20) NULL,
    address       TEXT NULL,
    start_date    DATE            NOT NULL,
    status        employee_status NOT NULL DEFAULT 'PROBATION',
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ NULL,
    CONSTRAINT fk_employees_companies FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_employees_branches FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fk_employees_departments FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_employees_positions FOREIGN KEY (position_id) REFERENCES positions (id)
);

CREATE UNIQUE INDEX uq_employees_company_code ON employees (company_id, employee_code);
CREATE UNIQUE INDEX uq_employees_email_company_id ON employees (company_id, email);
CREATE INDEX idx_employees_company_id ON employees (company_id);
CREATE INDEX idx_employees_branch_id ON employees (branch_id);
CREATE INDEX idx_employees_department_id ON employees (department_id);
CREATE INDEX idx_employees_position_id ON employees (position_id);
CREATE INDEX idx_employees_status ON employees (status);

-- Table: users

CREATE TABLE users
(
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT,
    employee_id BIGINT,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL,
    CONSTRAINT fk_users_companies FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_users_employees FOREIGN KEY (employee_id) REFERENCES employees (id)
);

-- Email unique theo company (mỗi company không trùng email)
CREATE UNIQUE INDEX uq_users_company_email
    ON users (company_id, email) WHERE deleted_at IS NULL;

-- System account email unique riêng (company_id = null)
CREATE UNIQUE INDEX uq_users_system_email
    ON users (email) WHERE company_id IS NULL AND deleted_at IS NULL;

-- Mỗi employee chỉ có 1 tài khoản
CREATE UNIQUE INDEX uq_users_employee
    ON users (employee_id) WHERE deleted_at IS NULL;

CREATE INDEX idx_users_company_id ON users (company_id);

-- Table: user_social_accounts

CREATE TABLE user_social_accounts
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    company_id  BIGINT       NOT NULL,
    provider    VARCHAR(50)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_user_social_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_social_company_id FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT uq_user_social_provider UNIQUE (company_id, provider, provider_id)
);

-- Table: attendances

CREATE TABLE attendances
(
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT            NOT NULL,
    employee_id    BIGINT            NOT NULL,
    work_date      DATE              NOT NULL,
    check_in_time  TIMESTAMPTZ NULL,
    check_out_time TIMESTAMPTZ NULL,
    worked_hours   NUMERIC(5, 2) NULL,
    checked_status checked_status    NOT NULL DEFAULT 'NOT_CHECKED',
    status         attendance_status NOT NULL DEFAULT 'ABSENT',
    created_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ NULL,
    CONSTRAINT fk_attendance_companies FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_attendance_employees FOREIGN KEY (employee_id) REFERENCES employees (id)
);

CREATE UNIQUE INDEX uq_attendance_employee_date ON attendances (employee_id, work_date);
CREATE INDEX idx_attendance_company_id ON attendances (company_id);
CREATE INDEX idx_attendance_employee_id ON attendances (employee_id);
CREATE INDEX idx_attendance_work_date ON attendances (work_date);
CREATE INDEX idx_attendance_status ON attendances (status);

-- Table: roles
CREATE TABLE roles
(
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_system   BOOLEAN      NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL,
    CONSTRAINT fk_role_companies FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE UNIQUE INDEX uq_role_company_id_name ON roles (company_id, name) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_roles_company_id ON roles (company_id, id);

-- Table: user_has_roles
CREATE TABLE user_has_roles
(
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT,
    user_id    BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,
    CONSTRAINT fk_user_roles_company_id FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Company user: không trùng role trong cùng company
CREATE UNIQUE INDEX uq_user_has_roles
    ON user_has_roles (company_id, user_id, role_id) WHERE company_id IS NOT NULL;

-- System account: không trùng role khi company_id null
CREATE UNIQUE INDEX uq_user_has_roles_system
    ON user_has_roles (user_id, role_id) WHERE company_id IS NULL;

CREATE INDEX idx_user_has_roles_company_user ON user_has_roles (company_id, user_id);

-- Table: permissions
CREATE TABLE permissions
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    module      VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL
);


-- Table: role_has_permissions
CREATE TABLE role_has_permissions
(
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_role_has_permissions ON role_has_permissions (role_id, permission_id);
CREATE INDEX idx_role_has_permissions_role_id ON role_has_permissions (role_id);

-- Table: refresh_tokens
CREATE TABLE refresh_tokens
(
    token       TEXT PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked     BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP          DEFAULT NOW()
);

-- Table: password_reset_tokens

CREATE TABLE password_reset_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    token       TEXT      NOT NULL,
    user_id     BIGINT    NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    used        BOOLEAN   NOT NULL DEFAULT FALSE
);

-- Table: company_verifications
CREATE TABLE company_verifications
(
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT    NOT NULL,
    token      VARCHAR   NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table: projects

CREATE TABLE projects
(
    id            BIGSERIAL PRIMARY KEY,
    company_id    BIGINT         NOT NULL,
    creator_id    BIGINT         NOT NULL,
    name          VARCHAR(255)   NOT NULL,
    key           VARCHAR(10)    NOT NULL,
    issue_counter BIGINT         NOT NULL DEFAULT 0,
    description   TEXT,
    status        project_status NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ             DEFAULT NOW(),
    updated_at    TIMESTAMPTZ             DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ,
    CONSTRAINT fk_project_company_id FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_creator_id FOREIGN KEY (creator_id) REFERENCES users (id),
    CONSTRAINT uq_project_company_key UNIQUE (company_id, key)
);

CREATE INDEX idx_project_company_id ON projects (company_id);

-- Table: project_roles

CREATE TABLE project_roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    description TEXT
);

-- Table: project_members

CREATE TABLE project_members
(
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT                NOT NULL,
    project_role_id BIGINT                NOT NULL,
    user_id         BIGINT                NOT NULL,
    status          project_member_status NOT NULL DEFAULT 'ACTIVE',
    invited_email   VARCHAR(255),
    joined_at       TIMESTAMPTZ                    DEFAULT NOW(),
    CONSTRAINT fk_pm_project_id FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_project_role_id FOREIGN KEY (project_role_id) REFERENCES project_roles (id),
    CONSTRAINT fk_pm_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_project_user UNIQUE (project_id, user_id)
);

CREATE INDEX idx_prj_member_prj_user ON project_members (project_id, user_id);

-- Table: project_invitations

CREATE TABLE project_invitations
(
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT                    NOT NULL,
    project_role_id BIGINT                    NOT NULL,
    inviter_id      BIGINT                    NOT NULL,
    email           VARCHAR(255),
    token           VARCHAR(255)              NOT NULL UNIQUE,
    status          project_invitation_status NOT NULL DEFAULT 'PENDING',
    expired_at      TIMESTAMPTZ               NOT NULL,
    created_at      TIMESTAMPTZ                        DEFAULT NOW(),
    updated_at      TIMESTAMPTZ                        DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT fk_pi_project_id FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_project_role_id FOREIGN KEY (project_role_id) REFERENCES project_roles (id),
    CONSTRAINT fk_pi_inviter_id FOREIGN KEY (inviter_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_prj_invitation_token ON project_invitations (token);

-- Table: sprints

CREATE TABLE sprints
(
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT        NOT NULL,
    name       VARCHAR(100)  NOT NULL,
    goal       TEXT,
    start_date TIMESTAMPTZ,
    end_date   TIMESTAMPTZ,
    status     sprint_status NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMPTZ            DEFAULT NOW(),
    updated_at TIMESTAMPTZ            DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_sprint_project_id FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);


-- Table: board_columns

CREATE TABLE board_columns
(
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT          NOT NULL,
    name       VARCHAR(100)    NOT NULL,
    position   INTEGER         NOT NULL,
    category   column_category NOT NULL DEFAULT 'TO_DO',
    created_at TIMESTAMPTZ              DEFAULT NOW(),
    updated_at TIMESTAMPTZ              DEFAULT NOW(),
    CONSTRAINT fk_bc_project_id FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT uq_project_column_position UNIQUE (project_id, position)
);

-- Table: tasks

CREATE TABLE tasks
(
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT        NOT NULL,
    project_id      BIGINT        NOT NULL,
    sprint_id       BIGINT,
    column_id       BIGINT        NOT NULL,
    parent_id       BIGINT,
    reporter_id     BIGINT        NOT NULL,
    assignee_id     BIGINT,
    task_key        VARCHAR(50)   NOT NULL UNIQUE,
    title           VARCHAR(255)  NOT NULL,
    description     TEXT,
    priority        task_priority NOT NULL DEFAULT 'MEDIUM',
    type            task_type     NOT NULL DEFAULT 'TASK',
    points          INTEGER,
    estimated_hours DOUBLE PRECISION,
    logged_hours    DOUBLE PRECISION       DEFAULT 0.0,
    position        DOUBLE PRECISION       DEFAULT 0.0,
    due_date        TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ            DEFAULT NOW(),
    updated_at      TIMESTAMPTZ            DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT fk_task_company_id FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_project_id FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_sprint_id FOREIGN KEY (sprint_id) REFERENCES sprints (id) ON DELETE SET NULL,
    CONSTRAINT fk_task_column_id FOREIGN KEY (column_id) REFERENCES board_columns (id),
    CONSTRAINT fk_task_parent_id FOREIGN KEY (parent_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_reporter_id FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_task_assignee_id FOREIGN KEY (assignee_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_tasks_project_id ON tasks (project_id);
CREATE INDEX idx_tasks_sprint_id ON tasks (sprint_id);
CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);

-- Table: task_comments

CREATE TABLE task_comments
(
    id         BIGSERIAL PRIMARY KEY,
    task_id    BIGINT NOT NULL,
    parent_id  BIGINT,
    author_id  BIGINT NOT NULL,
    content    TEXT   NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_tc_task_id FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_tc_parent_id FOREIGN KEY (parent_id) REFERENCES task_comments (id) ON DELETE CASCADE,
    CONSTRAINT fk_tc_author_id FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);


-- Table: task_attachments

CREATE TABLE task_attachments
(
    id          BIGSERIAL PRIMARY KEY,
    task_id     BIGINT       NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_url    TEXT         NOT NULL,
    file_size   BIGINT,
    uploaded_by BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ta_task_id FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_ta_uploader FOREIGN KEY (uploaded_by) REFERENCES users (id) ON DELETE CASCADE
);

-- Table: task_activities

CREATE TABLE task_activities
(
    id         BIGSERIAL PRIMARY KEY,
    task_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    action     VARCHAR(100) NOT NULL,
    old_value  TEXT,
    new_value  TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_tac_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_tac_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Table: task_worklogs

CREATE TABLE task_worklogs
(
    id               BIGSERIAL PRIMARY KEY,
    task_id          BIGINT           NOT NULL,
    user_id          BIGINT           NOT NULL,
    time_spent_hours DOUBLE PRECISION NOT NULL,
    description      TEXT,
    logged_at        TIMESTAMPTZ DEFAULT NOW(),
    created_at       TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT fk_tw_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_tw_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- Table: kpis

CREATE TABLE kpis
(
    id            BIGSERIAL PRIMARY KEY,
    company_id    BIGINT           NOT NULL,
    employee_id   BIGINT           NOT NULL,
    title         VARCHAR(255)     NOT NULL,
    description   TEXT,
    target_value  DOUBLE PRECISION NOT NULL,
    current_value DOUBLE PRECISION          DEFAULT 0.0,
    unit          VARCHAR(50)      NOT NULL,
    weight        DOUBLE PRECISION NOT NULL,
    start_date    DATE             NOT NULL,
    end_date      DATE             NOT NULL,
    status        kpi_status       NOT NULL DEFAULT 'IN_PROGRESS',
    created_at    TIMESTAMPTZ               DEFAULT NOW(),
    updated_at    TIMESTAMPTZ               DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ,
    CONSTRAINT fk_kpi_company_id FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_kpi_employee_id FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE
);

CREATE INDEX idx_kpi_employee_id ON kpis (employee_id);

-- Table: performance_reviews

CREATE TABLE performance_reviews
(
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT                    NOT NULL,
    employee_id    BIGINT                    NOT NULL,
    reviewer_id    BIGINT                    NOT NULL,
    review_period  VARCHAR(100)              NOT NULL,
    self_score     DOUBLE PRECISION,
    reviewer_score DOUBLE PRECISION,
    final_score    DOUBLE PRECISION,
    feedback       TEXT,
    status         performance_review_status NOT NULL DEFAULT 'DRAFT',
    created_at     TIMESTAMPTZ                        DEFAULT NOW(),
    updated_at     TIMESTAMPTZ                        DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ,
    CONSTRAINT fk_pr_company_id FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_employee_id FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id)
);

CREATE INDEX idx_perf_review_employee_id ON performance_reviews (employee_id);


-- Table: employee_educations

CREATE TABLE employee_educations
(
    id                   BIGSERIAL PRIMARY KEY,
    employee_id          BIGINT NOT NULL,
    degree_level         VARCHAR(255) NULL,
    school_name          VARCHAR(255) NULL,
    major                VARCHAR(255) NULL,
    start_year           INTEGER NULL,
    end_year             INTEGER NULL,
    gpa                  DOUBLE PRECISION NULL,
    certificate_file_url TEXT NULL,
    note                 TEXT NULL,
    created_at           TIMESTAMPTZ DEFAULT NOW(),
    updated_at           TIMESTAMPTZ DEFAULT NOW(),
    deleted_at           TIMESTAMPTZ,
    CONSTRAINT fk_employee_id_edu FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE
);


-- Table: employee_certifications

CREATE TABLE employee_certifications
(
    id                     BIGSERIAL PRIMARY KEY,
    employee_id            BIGINT       NOT NULL,
    name                   VARCHAR(255) NOT NULL,
    certification_code     VARCHAR(255) NULL,
    issued_by              VARCHAR(255) NULL,
    issued_date            DATE NULL,
    expired_date           DATE NULL,
    score_or_level         VARCHAR(255) NULL,
    certification_file_url TEXT NULL,
    created_at             TIMESTAMPTZ DEFAULT NOW(),
    updated_at             TIMESTAMPTZ DEFAULT NOW(),
    deleted_at             TIMESTAMPTZ,
    CONSTRAINT fk_employee_id_cert FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE
);


-- Table: employee_work_experiences

CREATE TABLE employee_work_experiences
(
    id                 BIGSERIAL PRIMARY KEY,
    employee_id        BIGINT NOT NULL,
    company_name       VARCHAR(255) NULL,
    position           VARCHAR(255) NULL,
    start_date         DATE NULL,
    end_date           DATE NULL,
    description        TEXT NULL,
    reason_for_leaving VARCHAR(255) NULL,
    created_at         TIMESTAMPTZ DEFAULT NOW(),
    updated_at         TIMESTAMPTZ DEFAULT NOW(),
    deleted_at         TIMESTAMPTZ,
    CONSTRAINT fk_employee_id_exp FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE
);


-- Table: employee_id_documents

CREATE TABLE employee_id_documents
(
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT      NOT NULL,
    id_type         VARCHAR(20) NOT NULL,
    id_number       VARCHAR(50) NOT NULL,
    issued_date     DATE NULL,
    issued_place    VARCHAR(255) NULL,
    expired_date    DATE NULL,
    front_image_url TEXT NULL,
    back_image_url  TEXT NULL,
    is_current      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ          DEFAULT NOW(),
    updated_at      TIMESTAMPTZ          DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT fk_employee_id_iddoc FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE
);


-- Table: leave_types

CREATE TABLE leave_types
(
    id                  BIGSERIAL PRIMARY KEY,
    company_id          BIGINT       NOT NULL,
    name                VARCHAR(100) NOT NULL,
    code                VARCHAR(50)  NOT NULL,
    max_days            NUMERIC(5, 2) NULL,
    is_paid             BOOLEAN      NOT NULL DEFAULT TRUE,
    requires_attachment BOOLEAN      NOT NULL DEFAULT FALSE,
    description         TEXT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ NULL,
    CONSTRAINT fk_leave_types_companies FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_leave_types_company_code ON leave_types (company_id, code) WHERE deleted_at IS NULL;
CREATE INDEX idx_leave_types_company ON leave_types (company_id);


-- Table: leave_balances

CREATE TABLE leave_balances
(
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT        NOT NULL,
    employee_id    BIGINT        NOT NULL,
    leave_type_id  BIGINT        NOT NULL,
    year           INTEGER       NOT NULL,
    allocated_days NUMERIC(5, 2) NOT NULL DEFAULT 12.00,
    used_days      NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    pending_days   NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ,
    CONSTRAINT fk_leave_balances_companies FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_balances_employees FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_balances_types FOREIGN KEY (leave_type_id) REFERENCES leave_types (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_leave_balances_emp_type_year ON leave_balances (employee_id, leave_type_id, year);
CREATE INDEX idx_leave_balances_company ON leave_balances (company_id);
CREATE INDEX idx_leave_balances_employee ON leave_balances (employee_id);

CREATE TABLE leave_requests
(
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT               NOT NULL,
    employee_id    BIGINT               NOT NULL,
    leave_type_id  BIGINT               NOT NULL,
    start_date     DATE                 NOT NULL,
    end_date       DATE                 NOT NULL,
    duration_days  NUMERIC(5, 2)        NOT NULL,
    session_type   leave_session_type   NOT NULL DEFAULT 'FULL_DAY',
    reason         TEXT                 NOT NULL,
    attachment_url TEXT NULL,
    status         leave_request_status NOT NULL DEFAULT 'PENDING',
    approver_id    BIGINT NULL,
    approver_note  TEXT NULL,
    approved_at    TIMESTAMPTZ NULL,
    created_at     TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ NULL,
    CONSTRAINT fk_leave_requests_companies FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_requests_employees FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_requests_types FOREIGN KEY (leave_type_id) REFERENCES leave_types (id),
    CONSTRAINT fk_leave_requests_approver FOREIGN KEY (approver_id) REFERENCES users (id)
);

CREATE INDEX idx_leave_requests_company ON leave_requests (company_id);
CREATE INDEX idx_leave_requests_employee ON leave_requests (employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests (status);
CREATE INDEX idx_leave_requests_dates ON leave_requests (start_date, end_date);


-- Enable RLS
-- Bật RLS
ALTER TABLE branches
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE employees
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE users
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendances
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE roles
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_has_roles
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE positions
    DISABLE ROW LEVEL SECURITY;
ALTER TABLE projects
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_social_accounts
    ENABLE ROW LEVEL SECURITY;

-- Policy cho từng bảng
CREATE
POLICY tenant_isolation ON branches
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
POLICY tenant_isolation ON employees
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
POLICY tenant_isolation ON users
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
POLICY tenant_isolation ON attendances
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
POLICY tenant_isolation ON roles
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
        OR (company_id IS NULL AND is_system = true)
    );

CREATE
POLICY tenant_isolation ON user_has_roles
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
POLICY tenant_isolation ON positions
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
POLICY tenant_isolation ON projects
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
POLICY tenant_isolation ON tasks
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
POLICY tenant_isolation ON user_social_accounts
    USING (
        current_setting('app.is_admin', true) = 'true'
        OR company_id = NULLIF(current_setting('app.company_id', true), '')::BIGINT
    );

CREATE
USER app_user WITH PASSWORD 'secret';
GRANT
SELECT,
INSERT
,
UPDATE,
DELETE
ON ALL TABLES IN SCHEMA public TO app_user;
-- Tables
GRANT
SELECT,
INSERT
,
UPDATE,
DELETE
ON ALL TABLES IN SCHEMA public TO app_user;

-- Sequences
GRANT
USAGE,
SELECT
ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Future tables
ALTER
DEFAULT PRIVILEGES IN SCHEMA public
    GRANT
SELECT,
INSERT
,
UPDATE,
DELETE
ON TABLES TO app_user;

-- Future sequences
ALTER
DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE,
SELECT
ON SEQUENCES TO app_user;
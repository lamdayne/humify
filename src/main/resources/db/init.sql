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

CREATE TYPE project_status AS ENUM (
    'PLANNING', -- Đang lập kế hoạch
    'IN_PROGRESS', -- Đang thực hiện
    'ON_HOLD', -- Tạm dừng
    'COMPLETED', -- Đã hoàn thành
    'CANCELLED' -- Đã bị hủy
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

-- Table: companies

CREATE TABLE companies
(
    id                   BIGSERIAL PRIMARY KEY,
    company_code         VARCHAR(50)    NOT NULL,
    name                 VARCHAR(255)   NOT NULL,
    field                VARCHAR(255)   NOT NULL,
    website              VARCHAR(255)   NULL,
    tax_code             VARCHAR(20)    NOT NULL,
    phone                VARCHAR(20)    NOT NULL,
    representative_email VARCHAR(255)   NOT NULL,
    status               company_status NOT NULL DEFAULT 'PENDING',
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMPTZ    NULL
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
    website                VARCHAR(255)  NULL,
    address                TEXT          NULL,
    status                 branch_status NOT NULL DEFAULT 'ACTIVE',
    standard_hours_per_day INTEGER       NOT NULL DEFAULT 8,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at             TIMESTAMPTZ   NULL,
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
    description TEXT         NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ  NULL,
    CONSTRAINT fk_departments_branches FOREIGN KEY (branch_id) REFERENCES branches (id)
);

CREATE INDEX idx_departments_branch_id ON departments (branch_id);

-- Table: positions

CREATE TABLE positions
(
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT         NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ  NULL,
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
    date_of_birth DATE            NULL,
    gender        gender          NULL,
    email         VARCHAR(255)    NULL,
    phone         VARCHAR(20)     NULL,
    address       TEXT            NULL,
    start_date    DATE            NOT NULL,
    status        employee_status NOT NULL DEFAULT 'PROBATION',
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ     NULL,
    CONSTRAINT fk_employees_companies FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_employees_branches FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fk_employees_departments FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_employees_positions FOREIGN KEY (position_id) REFERENCES positions (id)
);

CREATE UNIQUE INDEX uq_employees_company_code ON employees (company_id, employee_code);
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
    password    VARCHAR(255) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ  NULL,
    CONSTRAINT fk_users_companies FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_users_employees FOREIGN KEY (employee_id) REFERENCES employees (id)
);

-- Email unique theo company (mỗi company không trùng email)
CREATE UNIQUE INDEX uq_users_company_email
    ON users (company_id, email)
    WHERE deleted_at IS NULL;

-- System account email unique riêng (company_id = null)
CREATE UNIQUE INDEX uq_users_system_email
    ON users (email)
    WHERE company_id IS NULL AND deleted_at IS NULL;

-- Mỗi employee chỉ có 1 tài khoản
CREATE UNIQUE INDEX uq_users_employee
    ON users (employee_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_users_company_id ON users (company_id);

-- Table: attendances

CREATE TABLE attendances
(
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT            NOT NULL,
    employee_id    BIGINT            NOT NULL,
    work_date      DATE              NOT NULL,
    check_in_time  TIMESTAMPTZ       NULL,
    check_out_time TIMESTAMPTZ       NULL,
    worked_hours   NUMERIC(5, 2)     NULL,
    checked_status checked_status    NOT NULL DEFAULT 'NOT_CHECKED',
    status         attendance_status NOT NULL DEFAULT 'ABSENT',
    created_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ       NULL,
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
    deleted_at  TIMESTAMPTZ  NULL,
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
    ON user_has_roles (company_id, user_id, role_id)
    WHERE company_id IS NOT NULL;

-- System account: không trùng role khi company_id null
CREATE UNIQUE INDEX uq_user_has_roles_system
    ON user_has_roles (user_id, role_id)
    WHERE company_id IS NULL;

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
    deleted_at  TIMESTAMPTZ  NULL
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

-- Policy cho từng bảng
CREATE POLICY tenant_isolation ON branches
    USING (company_id = current_setting('app.company_id', true)::BIGINT);

CREATE POLICY tenant_isolation ON employees
    USING (company_id = current_setting('app.company_id', true)::BIGINT);

CREATE POLICY tenant_isolation ON users
    USING (
        company_id = current_setting('app.company_id', true)::BIGINT
        OR company_id IS NULL  -- system account
    );

CREATE POLICY tenant_isolation ON attendances
    USING (company_id = current_setting('app.company_id', true)::BIGINT);

CREATE POLICY tenant_isolation ON roles
    USING (
        company_id = current_setting('app.company_id', true)::BIGINT
        OR company_id IS NULL  -- system roles
    );

CREATE POLICY tenant_isolation ON user_has_roles
    USING (
        company_id = current_setting('app.company_id', true)::BIGINT
        OR company_id IS NULL  -- system account
    );

CREATE USER app_user WITH PASSWORD 'secret';
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
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

CREATE TYPE user_role AS ENUM (
    'SYS_ADMIN',
    'COMPANY_ADMIN',
    'MANAGER',
    'EMPLOYEE'
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
CREATE INDEX idx_employees_company_id ON employees (company_id);
CREATE INDEX idx_employees_branch_id ON employees (branch_id);
CREATE INDEX idx_employees_department_id ON employees (department_id);
CREATE INDEX idx_employees_position_id ON employees (position_id);
CREATE INDEX idx_employees_status ON employees (status);

-- Table: users

CREATE TABLE users
(
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT       NOT NULL,
    employee_id BIGINT       NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        user_role    NOT NULL DEFAULT 'EMPLOYEE',
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL,
    CONSTRAINT fk_users_companies FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_users_employees FOREIGN KEY (employee_id) REFERENCES employees (id)
);

CREATE UNIQUE INDEX uq_users_email ON users (email);
CREATE UNIQUE INDEX uq_users_employee ON users (employee_id);
CREATE INDEX idx_users_company_id ON users (company_id);
CREATE INDEX idx_users_role ON users (role);

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

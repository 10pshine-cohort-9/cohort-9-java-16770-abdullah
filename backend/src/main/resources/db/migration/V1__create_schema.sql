CREATE TABLE users (
    id bigint IDENTITY NOT NULL,
    email varchar(255),
    phone_number varchar(255),
    password_hash varchar(255) NOT NULL,
    created_at datetimeoffset(7) NOT NULL,
    updated_at datetimeoffset(7) NOT NULL,
    PRIMARY KEY (id)
);

-- Email and phone are both optional (you can register with either one), so
-- these can't be plain UNIQUE constraints: SQL Server allows only a single
-- NULL per unique column, which would reject the second phone-only signup.
-- Filtered indexes apply the uniqueness only to rows where the value is set.
CREATE UNIQUE INDEX uk_users_email
    ON users (email) WHERE email IS NOT NULL;

CREATE UNIQUE INDEX uk_users_phone_number
    ON users (phone_number) WHERE phone_number IS NOT NULL;

CREATE TABLE contacts (
    id bigint IDENTITY NOT NULL,
    owner_id bigint NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    title varchar(255),
    created_at datetimeoffset(7) NOT NULL,
    updated_at datetimeoffset(7) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_contacts_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE contact_emails (
    id bigint IDENTITY NOT NULL,
    contact_id bigint NOT NULL,
    label varchar(255) NOT NULL CHECK (label IN ('WORK', 'HOME', 'PERSONAL', 'OTHER')),
    email varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_contact_emails_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE
);

CREATE TABLE contact_phones (
    id bigint IDENTITY NOT NULL,
    contact_id bigint NOT NULL,
    label varchar(255) NOT NULL CHECK (label IN ('WORK', 'HOME', 'PERSONAL', 'OTHER')),
    phone_number varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_contact_phones_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE
);

-- Every contact query filters by owner, and the child rows are always loaded
-- by contact id, so index those foreign keys.
CREATE INDEX ix_contacts_owner_id ON contacts (owner_id);
CREATE INDEX ix_contact_emails_contact_id ON contact_emails (contact_id);
CREATE INDEX ix_contact_phones_contact_id ON contact_phones (contact_id);

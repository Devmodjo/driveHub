-- ============================================================
-- Template tables métier par tenant (auto-école)
-- Exécuté par TenantProvisioningService lors de l'approbation
-- ============================================================

-- ─── MONITORS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS monitors (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    date_of_birth timestamp(6) without time zone NOT NULL,
    gender character varying(255) NOT NULL,
    nationality character varying(255) NOT NULL,
    phone_number character varying(255) NOT NULL,
    residence_city character varying(255) NOT NULL,
    driving_school_id uuid,
    user_id uuid,
    CONSTRAINT monitors_pkey PRIMARY KEY (id),
    CONSTRAINT monitors_gender_check CHECK (
        gender = ANY (ARRAY['MALE', 'FEMALE'])
    )
);

-- ─── STUDENTS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS students (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    cni_recto_url character varying(255),
    cni_verso_url character varying(255),
    date_of_birth timestamp(6) without time zone NOT NULL,
    gender character varying(255) NOT NULL,
    license_category character varying(255),
    nationality character varying(255) NOT NULL,
    phone_number character varying(255) NOT NULL,
    residence_city character varying(255) NOT NULL,
    user_id uuid,
    CONSTRAINT students_pkey PRIMARY KEY (id),
    CONSTRAINT students_gender_check CHECK (
        gender = ANY (ARRAY['MALE', 'FEMALE'])
    ),
    CONSTRAINT students_license_category_check CHECK (
        license_category = ANY (ARRAY['A', 'B', 'C', 'D', 'E', 'G'])
    )
);

-- ─── DRIVING SCHOOL ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS driving_school (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    address character varying(255) NOT NULL,
    city character varying(255),
    country character varying(255),
    created_at date,
    description character varying(255),
    driving_school_status character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    phone_number character varying(255) NOT NULL,
    website_url character varying(255),
    whatsapp_number character varying(255),
    user_id uuid,
    CONSTRAINT driving_school_pkey PRIMARY KEY (id),
    CONSTRAINT driving_school_driving_school_status_check CHECK (
        driving_school_status = ANY (ARRAY[
            'APPROVED', 'ACTIVE', 'PENDING', 'SUSPENDED', 'REJECTED'
        ])
    )
);

-- ─── VEHICLES ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vehicles (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    matriculation character varying(255) NOT NULL,
    model character varying(255) NOT NULL,
    state character varying(255) NOT NULL,
    driving_school_id uuid,
    CONSTRAINT vehicles_pkey PRIMARY KEY (id),
    CONSTRAINT vehicles_state_check CHECK (
        state = ANY (ARRAY['DISPOSABLE', 'PANNE', 'MAINTENANCE'])
    ),
    CONSTRAINT fkgcbpqg4rmsiqr80i1y5bcm4ri
        FOREIGN KEY (driving_school_id) REFERENCES driving_school(id)
);

-- ─── COURSES ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS courses (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    content character varying(255) NOT NULL,
    created_at date,
    title character varying(255) NOT NULL,
    driving_school_id uuid,
    CONSTRAINT courses_pkey PRIMARY KEY (id),
    CONSTRAINT fkjfgk4pl7ghl7ltf0ew7r58f1d
        FOREIGN KEY (driving_school_id) REFERENCES driving_school(id)
);

-- ─── EXAMS ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS exams (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    category character varying(255),
    date_exams timestamp(6) without time zone NOT NULL,
    driving_school_id uuid,
    CONSTRAINT exams_pkey PRIMARY KEY (id),
    CONSTRAINT exams_category_check CHECK (
        category = ANY (ARRAY['A', 'B', 'C', 'D', 'E', 'G'])
    ),
    CONSTRAINT fkrma02beu634nioxgyix1mis9n
        FOREIGN KEY (driving_school_id) REFERENCES driving_school(id)
);

-- ─── EXAMS INSCRIPTION ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS exams_inscription (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    inscription_status character varying(255),
    registered_at date,
    exams_id uuid,
    student_id uuid,
    CONSTRAINT exams_inscription_pkey PRIMARY KEY (id),
    CONSTRAINT exams_inscription_inscription_status_check CHECK (
        inscription_status = ANY (ARRAY['INSCRIT', 'REFUSE'])
    ),
    CONSTRAINT fk4di0265x8dvrcv345n1xye0sp
        FOREIGN KEY (exams_id) REFERENCES exams(id),
    CONSTRAINT fkbwvh2f75nt4v1amne9cti4ohd
        FOREIGN KEY (student_id) REFERENCES students(id)
);

-- ─── RESERVATIONS ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservations (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    date_time timestamp(6) without time zone,
    reservation_status character varying(255) NOT NULL,
    types character varying(255) NOT NULL,
    driving_school_id uuid,
    monitor_id uuid,
    student_id uuid,
    CONSTRAINT reservations_pkey PRIMARY KEY (id),
    CONSTRAINT reservations_reservation_status_check CHECK (
        reservation_status = ANY (ARRAY['PENDING', 'CONFIRMED', 'CANCELLED'])
    ),
    CONSTRAINT reservations_types_check CHECK (
        types = ANY (ARRAY['CONDUITE', 'RENDEZVOUS', 'ADMIN'])
    ),
    CONSTRAINT fk1vod9xw6vvsnunskskgp81ymn
        FOREIGN KEY (monitor_id) REFERENCES monitors(id),
    CONSTRAINT fkixwcf2v94jc6p46128vbochlu
        FOREIGN KEY (driving_school_id) REFERENCES driving_school(id),
    CONSTRAINT fknlgg22885nfyspmen9jj0jcpp
        FOREIGN KEY (student_id) REFERENCES students(id)
);

-- ─── PAYMENTS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payments (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    amount double precision NOT NULL,
    date_payment date NOT NULL,
    method character varying(255) NOT NULL,
    motif character varying(255) NOT NULL,
    payment_status character varying(255) NOT NULL,
    driving_school_id uuid,
    student_id uuid,
    CONSTRAINT payments_pkey PRIMARY KEY (id),
    CONSTRAINT payments_method_check CHECK (
        method = ANY (ARRAY['MOMO', 'OM', 'CASH'])
    ),
    CONSTRAINT payments_motif_check CHECK (
        motif = ANY (ARRAY['INSCRIPTION', 'EXAMS'])
    ),
    CONSTRAINT payments_payment_status_check CHECK (
        payment_status = ANY (ARRAY['PENDING', 'VALIDATE', 'REJECTED'])
    ),
    CONSTRAINT fk6ooq278k2bs5xi8t5o6oort1v
        FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fkrynle6ugyodc808swe8oygj4p
        FOREIGN KEY (driving_school_id) REFERENCES driving_school(id)
);

-- ─── SCHOOL JOIN REQUEST ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS school_join_request (
    id uuid NOT NULL,
    created_on timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    deleted_at timestamp(6) without time zone,
    last_update_on timestamp(6) without time zone,
    status smallint NOT NULL,
    driving_school_id uuid NOT NULL,
    join_status character varying(255) NOT NULL,
    requested_role character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT school_join_request_pkey PRIMARY KEY (id),
    CONSTRAINT school_join_request_join_status_check CHECK (
        join_status = ANY (ARRAY['PENDING', 'APPROVED', 'REJECTED'])
    ),
    CONSTRAINT school_join_request_requested_role_check CHECK (
        requested_role = ANY (ARRAY['MONITOR', 'STUDENT'])
    )
);
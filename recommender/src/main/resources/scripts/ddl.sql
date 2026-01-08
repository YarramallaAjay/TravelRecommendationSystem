
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pgcrypto for hashing (useful for idempotency)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- DROP EXISTING TABLES (in reverse dependency order)
-- ============================================================================

DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS seat_locks CASCADE;
DROP TABLE IF EXISTS idempotency_keys CASCADE;
DROP TABLE IF EXISTS tickets CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS train_stations CASCADE;
DROP TABLE IF EXISTS coaches CASCADE;
DROP TABLE IF EXISTS journeys CASCADE;
DROP TABLE IF EXISTS trains CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop existing views
DROP VIEW IF EXISTS active_bookings CASCADE;
DROP VIEW IF EXISTS train_availability_summary CASCADE;
DROP VIEW IF EXISTS user_booking_history CASCADE;

-- Drop existing functions
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;
DROP FUNCTION IF EXISTS cleanup_expired_idempotency_keys() CASCADE;
DROP FUNCTION IF EXISTS cleanup_expired_seat_locks() CASCADE;
DROP FUNCTION IF EXISTS release_expired_ticket_blocks() CASCADE;

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       contact VARCHAR(15) NOT NULL UNIQUE,
                       age INTEGER,
                       address TEXT,
                       city VARCHAR(50),
                       state VARCHAR(50),
                       pincode VARCHAR(10),
                       password_hash VARCHAR(255) NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       preferred_coach_class VARCHAR(50),
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP WITH TIME ZONE,

                       CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED')),
                       CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
                       CONSTRAINT chk_contact_format CHECK (contact ~* '^\+?[0-9]{10,15}$'),
                       CONSTRAINT chk_age_range CHECK (age IS NULL OR (age >= 0 AND age <= 150))
);

-- Indexes for users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_contact ON users(contact);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);

COMMENT ON TABLE users IS 'Stores user account information and preferences';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password';
COMMENT ON COLUMN users.preferred_coach_class IS 'User preferred coach class for recommendations';

-- ============================================================================
-- TRAINS TABLE
-- ============================================================================
CREATE TABLE trains (
                        id BIGSERIAL PRIMARY KEY,
                        train_number VARCHAR(20) NOT NULL UNIQUE,
                        train_name VARCHAR(100) NOT NULL,
                        train_type VARCHAR(20) NOT NULL,
                        source_station VARCHAR(100) NOT NULL,
                        destination_station VARCHAR(100) NOT NULL,
                        departure_time TIME NOT NULL,
                        arrival_time TIME NOT NULL,
                        journey_duration_minutes INTEGER NOT NULL,
                        operating_days VARCHAR(50) NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI,SAT,SUN',
                        is_active BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT chk_train_type CHECK (train_type IN (
                                                                        'EXPRESS', 'SUPERFAST', 'MAIL', 'PASSENGER',
                                                                        'RAJDHANI', 'SHATABDI', 'DURONTO', 'VANDE_BHARAT'
                            )),
                        CONSTRAINT chk_journey_duration CHECK (journey_duration_minutes > 0),
                        CONSTRAINT chk_different_stations CHECK (source_station != destination_station)
);

-- Indexes for trains table
CREATE INDEX idx_trains_number ON trains(train_number);
CREATE INDEX idx_trains_route ON trains(source_station, destination_station);
CREATE INDEX idx_trains_source ON trains(source_station);
CREATE INDEX idx_trains_destination ON trains(destination_station);
CREATE INDEX idx_trains_type ON trains(train_type);
CREATE INDEX idx_trains_active ON trains(is_active) WHERE is_active = TRUE;

COMMENT ON TABLE trains IS 'Master data for all trains in the system';
COMMENT ON COLUMN trains.operating_days IS 'Comma-separated days: MON,TUE,WED,THU,FRI,SAT,SUN';

-- ============================================================================
-- TRAIN STATIONS TABLE (Intermediate stops)
-- ============================================================================
CREATE TABLE train_stations (
                                id BIGSERIAL PRIMARY KEY,
                                train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE,
                                station_code VARCHAR(100) NOT NULL,
                                station_name VARCHAR(100) NOT NULL,
                                station_order INTEGER NOT NULL,
                                arrival_time TIME NOT NULL,
                                departure_time TIME NOT NULL,
                                halt_time_minutes INTEGER DEFAULT 0,
                                distance_from_source DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
                                platform VARCHAR(10),
                                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT chk_station_order CHECK (station_order > 0),
                                CONSTRAINT chk_halt_time CHECK (halt_time_minutes >= 0),
                                CONSTRAINT chk_distance CHECK (distance_from_source >= 0),
                                CONSTRAINT uq_train_station_order UNIQUE(train_id, station_order),
                                CONSTRAINT uq_train_station_code UNIQUE(train_id, station_code)
);

-- Indexes for train_stations table
CREATE INDEX idx_train_stations_train ON train_stations(train_id);
CREATE INDEX idx_train_stations_order ON train_stations(train_id, station_order);
CREATE INDEX idx_train_stations_code ON train_stations(station_code);

COMMENT ON TABLE train_stations IS 'Intermediate stops for each train route';
COMMENT ON COLUMN train_stations.station_order IS 'Sequence number in the route (1, 2, 3...)';

-- ============================================================================
-- COACHES TABLE
-- ============================================================================
CREATE TABLE coaches (
                         id BIGSERIAL PRIMARY KEY,
                         train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE,
                         coach_number VARCHAR(10) NOT NULL,
                         coach_class VARCHAR(20) NOT NULL,
                         total_seats INTEGER NOT NULL,
                         available_seats INTEGER NOT NULL,
                         base_fare DECIMAL(10, 2) NOT NULL,
                         has_ac BOOLEAN DEFAULT FALSE,
                         has_charging BOOLEAN DEFAULT FALSE,
                         has_wifi BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT chk_coach_class CHECK (coach_class IN (
                                                                           'FIRST_AC', 'SECOND_AC', 'THIRD_AC', 'SLEEPER',
                                                                           'CHAIR_CAR', 'EXECUTIVE_CHAIR', 'GENERAL'
                             )),
                         CONSTRAINT chk_total_seats CHECK (total_seats > 0 AND total_seats <= 200),
                         CONSTRAINT chk_available_seats CHECK (available_seats >= 0 AND available_seats <= total_seats),
                         CONSTRAINT chk_base_fare CHECK (base_fare >= 0),
                         CONSTRAINT uq_train_coach_number UNIQUE(train_id, coach_number)
);

-- Indexes for coaches table
CREATE INDEX idx_coaches_train ON coaches(train_id);
CREATE INDEX idx_coaches_class ON coaches(coach_class);
CREATE INDEX idx_coaches_availability ON coaches(train_id, available_seats) WHERE available_seats > 0;

COMMENT ON TABLE coaches IS 'Coach/compartment details and seat availability';
COMMENT ON COLUMN coaches.available_seats IS 'Real-time available seat count, updated frequently';

-- ============================================================================
-- JOURNEYS TABLE
-- ============================================================================
CREATE TABLE journeys (
                          id BIGSERIAL PRIMARY KEY,
                          booking_id VARCHAR(100) NOT NULL UNIQUE,
                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          source_station VARCHAR(100) NOT NULL,
                          destination_station VARCHAR(100) NOT NULL,
                          journey_date DATE NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          journey_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE_TRAIN',
                          total_fare DECIMAL(10, 2),
                          total_travel_time_minutes INTEGER,
                          total_layover_minutes INTEGER DEFAULT 0,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          confirmed_at TIMESTAMP WITH TIME ZONE,
                          cancelled_at TIMESTAMP WITH TIME ZONE,
                          version BIGINT DEFAULT 0,

                          CONSTRAINT chk_journey_status CHECK (status IN (
                                                                          'DRAFT', 'AVAILABILITY_CHECK', 'SEATS_BLOCKED',
                                                                          'PAYMENT_PENDING', 'PAYMENT_FAILED', 'CONFIRMED',
                                                                          'CANCELLED', 'COMPLETED'
                              )),
                          CONSTRAINT chk_journey_type CHECK (journey_type IN ('SINGLE_TRAIN', 'MULTI_TRAIN')),
                          CONSTRAINT chk_total_fare CHECK (total_fare IS NULL OR total_fare >= 0),
                          CONSTRAINT chk_travel_time CHECK (total_travel_time_minutes IS NULL OR total_travel_time_minutes > 0),
                          CONSTRAINT chk_layover CHECK (total_layover_minutes >= 0)
);

-- Indexes for journeys table
CREATE INDEX idx_journeys_booking_id ON journeys(booking_id);
CREATE INDEX idx_journeys_user ON journeys(user_id);
CREATE INDEX idx_journeys_status ON journeys(status);
CREATE INDEX idx_journeys_date ON journeys(journey_date);
CREATE INDEX idx_journeys_user_date ON journeys(user_id, journey_date);
CREATE INDEX idx_journeys_created ON journeys(created_at DESC);

COMMENT ON TABLE journeys IS 'User journey bookings (can span multiple trains)';
COMMENT ON COLUMN journeys.booking_id IS 'User-facing booking reference number';
COMMENT ON COLUMN journeys.version IS 'Optimistic locking version for concurrent updates';

-- ============================================================================
-- TICKETS TABLE
-- ============================================================================
CREATE TABLE tickets (
                         id BIGSERIAL PRIMARY KEY,
                         pnr_number VARCHAR(50) NOT NULL UNIQUE,
                         journey_id BIGINT NOT NULL REFERENCES journeys(id) ON DELETE CASCADE,
                         train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE RESTRICT,
                         coach_id BIGINT NOT NULL REFERENCES coaches(id) ON DELETE RESTRICT,
                         seat_number VARCHAR(10) NOT NULL,
                         passenger_name VARCHAR(100) NOT NULL,
                         passenger_age INTEGER NOT NULL,
                         passenger_gender VARCHAR(10) NOT NULL,
                         boarding_station VARCHAR(100) NOT NULL,
                         destination_station VARCHAR(100) NOT NULL,
                         journey_date DATE NOT NULL,
                         departure_time TIME NOT NULL,
                         arrival_time TIME NOT NULL,
                         fare DECIMAL(10, 2) NOT NULL,
                         status VARCHAR(20) NOT NULL,
                         booked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         confirmed_at TIMESTAMP WITH TIME ZONE,
                         cancelled_at TIMESTAMP WITH TIME ZONE,
                         blocked_at TIMESTAMP WITH TIME ZONE,
                         block_expires_at TIMESTAMP WITH TIME ZONE,
                         external_booking_reference VARCHAR(100),
                         version BIGINT DEFAULT 0,

                         CONSTRAINT chk_ticket_status CHECK (status IN (
                                                                        'DRAFT', 'CHECKING_AVAILABILITY', 'AVAILABLE', 'BLOCKED',
                                                                        'BLOCK_EXPIRED', 'CONFIRMED', 'WAIT_LISTED', 'CANCELLED', 'REFUNDED'
                             )),
                         CONSTRAINT chk_passenger_gender CHECK (passenger_gender IN ('MALE', 'FEMALE', 'OTHER')),
                         CONSTRAINT chk_passenger_age CHECK (passenger_age >= 0 AND passenger_age <= 100),
                         CONSTRAINT chk_ticket_fare CHECK (fare >= 0),
                         CONSTRAINT uq_seat_booking UNIQUE(train_id, coach_id, seat_number, journey_date)
);

-- Indexes for tickets table
CREATE INDEX idx_tickets_pnr ON tickets(pnr_number);
CREATE INDEX idx_tickets_journey ON tickets(journey_id);
CREATE INDEX idx_tickets_train_date ON tickets(train_id, journey_date);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_blocked ON tickets(status, block_expires_at)
    WHERE status = 'BLOCKED';
CREATE INDEX idx_tickets_seat ON tickets(train_id, coach_id, seat_number, journey_date);

COMMENT ON TABLE tickets IS 'Individual tickets for each train segment';
COMMENT ON COLUMN tickets.pnr_number IS 'Unique PNR from external booking platform';
COMMENT ON COLUMN tickets.block_expires_at IS 'Auto-release time if payment not completed (typically 3-4 minutes)';
COMMENT ON COLUMN tickets.external_booking_reference IS 'Reference ID from external booking platform API';

-- ============================================================================
-- TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              transaction_id VARCHAR(100) NOT NULL UNIQUE,
                              booking_id VARCHAR(100) NOT NULL,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              journey_id BIGINT NOT NULL REFERENCES journeys(id) ON DELETE CASCADE,
                              amount DECIMAL(10, 2) NOT NULL,
                              currency VARCHAR(3) DEFAULT 'INR',
                              status VARCHAR(20) NOT NULL,
                              payment_method VARCHAR(20),
                              payment_gateway VARCHAR(100),
                              failure_reason TEXT,
                              gateway_response TEXT,
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              completed_at TIMESTAMP WITH TIME ZONE,
                              expires_at TIMESTAMP WITH TIME ZONE,
                              version BIGINT DEFAULT 0,

                              CONSTRAINT chk_transaction_status CHECK (status IN (
                                                                                  'INITIATED', 'PENDING', 'PROCESSING', 'SUCCESS',
                                                                                  'FAILED', 'CANCELLED', 'REFUNDED', 'REFUND_PENDING', 'EXPIRED'
                                  )),
                              CONSTRAINT chk_payment_method CHECK (payment_method IN (
                                                                                      'CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING', 'WALLET', 'EMI'
                                  )),
                              CONSTRAINT chk_amount CHECK (amount >= 0),
                              CONSTRAINT chk_currency CHECK (currency IN ('INR', 'USD', 'EUR', 'GBP'))
);

-- Indexes for transactions table
CREATE INDEX idx_transactions_transaction_id ON transactions(transaction_id);
CREATE INDEX idx_transactions_booking ON transactions(booking_id);
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_journey ON transactions(journey_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created ON transactions(created_at DESC);

COMMENT ON TABLE transactions IS 'Payment transaction records';
COMMENT ON COLUMN transactions.expires_at IS 'Payment window expiry (typically 3-4 minutes from creation)';
COMMENT ON COLUMN transactions.gateway_response IS 'Raw JSON response from payment gateway for debugging';

-- ============================================================================
-- IDEMPOTENCY KEYS TABLE
-- ============================================================================
CREATE TABLE idempotency_keys (
                                  id BIGSERIAL PRIMARY KEY,
                                  idempotency_key VARCHAR(100) NOT NULL UNIQUE,
                                  request_hash VARCHAR(256) NOT NULL,
                                  response_body TEXT,
                                  status VARCHAR(20) NOT NULL,
                                  operation_type VARCHAR(50) NOT NULL,
                                  entity_id VARCHAR(100),
                                  error_message TEXT,
                                  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                  version BIGINT DEFAULT 0,

                                  CONSTRAINT chk_idempotency_status CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
                                  CONSTRAINT chk_expires_at CHECK (expires_at > created_at)
);

-- Indexes for idempotency_keys table
CREATE UNIQUE INDEX idx_idempotency_key ON idempotency_keys(idempotency_key);
CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
CREATE INDEX idx_idempotency_status ON idempotency_keys(status);
CREATE INDEX idx_idempotency_operation ON idempotency_keys(operation_type);

COMMENT ON TABLE idempotency_keys IS 'Ensures idempotent API operations to prevent duplicate requests';
COMMENT ON COLUMN idempotency_keys.request_hash IS 'SHA-256 hash of request body to detect duplicate requests with different data';
COMMENT ON COLUMN idempotency_keys.response_body IS 'Cached response for completed operations';

-- ============================================================================
-- SEAT LOCKS TABLE
-- ============================================================================
CREATE TABLE seat_locks (
                            id BIGSERIAL PRIMARY KEY,
                            lock_key VARCHAR(200) NOT NULL UNIQUE,
                            train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE,
                            seat_number VARCHAR(10) NOT NULL,
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            booking_id VARCHAR(100) NOT NULL,
                            status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                            locked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

                            CONSTRAINT chk_lock_status CHECK (status IN ('ACTIVE', 'RELEASED', 'EXPIRED')),
                            CONSTRAINT chk_lock_expires CHECK (expires_at > locked_at)
);

-- Indexes for seat_locks table
CREATE UNIQUE INDEX idx_seat_locks_key ON seat_locks(lock_key);
CREATE INDEX idx_seat_locks_train_seat ON seat_locks(train_id, seat_number);
CREATE INDEX idx_seat_locks_expires ON seat_locks(expires_at);
CREATE INDEX idx_seat_locks_booking ON seat_locks(booking_id);
CREATE INDEX idx_seat_locks_status ON seat_locks(status) WHERE status = 'ACTIVE';

COMMENT ON TABLE seat_locks IS 'Distributed locks for seat blocking during payment window';
COMMENT ON COLUMN seat_locks.lock_key IS 'Redis-compatible lock key format: lock:seat:{trainId}:{seatId}:{date}';

-- ============================================================================
-- NOTIFICATIONS TABLE
-- ============================================================================
CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               type VARCHAR(50) NOT NULL,
                               subject VARCHAR(200) NOT NULL,
                               message TEXT NOT NULL,
                               channel VARCHAR(20) NOT NULL,
                               status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                               related_entity_id VARCHAR(100),
                               error_message TEXT,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               sent_at TIMESTAMP WITH TIME ZONE,
                               read_at TIMESTAMP WITH TIME ZONE,

                               CONSTRAINT chk_notification_type CHECK (type IN (
                                                                                'BOOKING_CONFIRMATION', 'PAYMENT_SUCCESS', 'PAYMENT_FAILED',
                                                                                'SEAT_BLOCKED', 'BOOKING_CANCELLED', 'REFUND_INITIATED',
                                                                                'JOURNEY_REMINDER'
                                   )),
                               CONSTRAINT chk_notification_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
                               CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ'))
);

-- Indexes for notifications table
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_type ON notifications(type);

COMMENT ON TABLE notifications IS 'User notifications (email, SMS, push, in-app)';

-- ============================================================================
-- TRIGGERS FOR UPDATED_AT TIMESTAMP
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to tables with updated_at column
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_trains_updated_at
    BEFORE UPDATE ON trains
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_coaches_updated_at
    BEFORE UPDATE ON coaches
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at
    BEFORE UPDATE ON transactions
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- CLEANUP FUNCTIONS
-- ============================================================================

-- Function to clean up expired idempotency keys
CREATE OR REPLACE FUNCTION cleanup_expired_idempotency_keys()
    RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM idempotency_keys
    WHERE expires_at < CURRENT_TIMESTAMP;

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_expired_idempotency_keys() IS
    'Deletes idempotency keys older than 24 hours. Run as scheduled job.';

-- Function to clean up expired seat locks
CREATE OR REPLACE FUNCTION cleanup_expired_seat_locks()
    RETURNS INTEGER AS $$
DECLARE
    expired_count INTEGER;
BEGIN
    UPDATE seat_locks
    SET status = 'EXPIRED'
    WHERE status = 'ACTIVE'
      AND expires_at < CURRENT_TIMESTAMP;

    GET DIAGNOSTICS expired_count = ROW_COUNT;
    RETURN expired_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_expired_seat_locks() IS
    'Marks expired seat locks. Run every minute as scheduled job.';

-- Function to release expired ticket blocks and update coach availability
CREATE OR REPLACE FUNCTION release_expired_ticket_blocks()
    RETURNS INTEGER AS $$
DECLARE
    expired_count INTEGER;
BEGIN
    -- Mark tickets as expired
    UPDATE tickets
    SET status = 'BLOCK_EXPIRED'
    WHERE status = 'BLOCKED'
      AND block_expires_at < CURRENT_TIMESTAMP;

    GET DIAGNOSTICS expired_count = ROW_COUNT;

    -- Increase available_seats in coaches for expired blocks
    UPDATE coaches c
    SET available_seats = available_seats + subq.count
    FROM (
             SELECT coach_id, COUNT(*) as count
             FROM tickets
             WHERE status = 'BLOCK_EXPIRED'
               AND block_expires_at >= CURRENT_TIMESTAMP - INTERVAL '1 minute'
             GROUP BY coach_id
         ) subq
    WHERE c.id = subq.coach_id;

    RETURN expired_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION release_expired_ticket_blocks() IS
    'Releases expired ticket blocks and restores seat availability. Run every minute.';

-- ============================================================================
-- VIEWS FOR COMMON QUERIES
-- ============================================================================

-- View for active bookings with user and journey details
CREATE OR REPLACE VIEW active_bookings AS
SELECT
    j.id AS journey_id,
    j.booking_id,
    u.id AS user_id,
    u.name AS user_name,
    u.email AS user_email,
    u.contact AS user_contact,
    j.source_station,
    j.destination_station,
    j.journey_date,
    j.status,
    j.journey_type,
    j.total_fare,
    j.total_travel_time_minutes,
    j.created_at,
    j.confirmed_at,
    COUNT(t.id) AS ticket_count
FROM journeys j
         JOIN users u ON j.user_id = u.id
         LEFT JOIN tickets t ON j.id = t.journey_id
WHERE j.status IN ('SEATS_BLOCKED', 'PAYMENT_PENDING', 'CONFIRMED')
GROUP BY j.id, u.id, u.name, u.email, u.contact;

COMMENT ON VIEW active_bookings IS 'Shows all active bookings with user details';

-- View for train availability summary
CREATE OR REPLACE VIEW train_availability_summary AS
SELECT
    t.id AS train_id,
    t.train_number,
    t.train_name,
    t.source_station,
    t.destination_station,
    c.coach_class,
    SUM(c.total_seats) AS total_seats,
    SUM(c.available_seats) AS available_seats,
    MIN(c.base_fare) AS min_fare,
    MAX(c.base_fare) AS max_fare
FROM trains t
         JOIN coaches c ON t.id = c.train_id
WHERE t.is_active = TRUE
GROUP BY t.id, t.train_number, t.train_name, t.source_station,
         t.destination_station, c.coach_class;

COMMENT ON VIEW train_availability_summary IS 'Aggregated seat availability by train and coach class';

-- View for user booking history
CREATE OR REPLACE VIEW user_booking_history AS
SELECT
    u.id AS user_id,
    u.name AS user_name,
    j.booking_id,
    j.journey_date,
    j.source_station,
    j.destination_station,
    j.status,
    j.total_fare,
    j.created_at,
    j.confirmed_at,
    j.cancelled_at,
    tr.status AS transaction_status,
    tr.payment_method
FROM users u
         JOIN journeys j ON u.id = j.user_id
         LEFT JOIN transactions tr ON j.id = tr.journey_id
ORDER BY j.created_at DESC;

COMMENT ON VIEW user_booking_history IS 'Complete booking history for users';

-- ============================================================================
-- UTILITY FUNCTIONS
-- ============================================================================

-- Function to generate booking ID
CREATE OR REPLACE FUNCTION generate_booking_id()
    RETURNS VARCHAR(100) AS $$
BEGIN
    RETURN 'BKG-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD') || '-' ||
           UPPER(SUBSTRING(MD5(RANDOM()::TEXT) FROM 1 FOR 8));
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generate_booking_id() IS 'Generates unique booking ID: BKG-YYYYMMDD-XXXXXXXX';

-- ============================================================================
-- VERIFICATION QUERIES (Run these after schema creation)
-- ============================================================================

-- List all tables
DO $$
    BEGIN
        RAISE NOTICE 'Tables created successfully:';
        RAISE NOTICE '  - users';
        RAISE NOTICE '  - trains';
        RAISE NOTICE '  - train_stations';
        RAISE NOTICE '  - coaches';
        RAISE NOTICE '  - journeys';
        RAISE NOTICE '  - tickets';
        RAISE NOTICE '  - transactions';
        RAISE NOTICE '  - idempotency_keys';
        RAISE NOTICE '  - seat_locks';
        RAISE NOTICE '  - notifications';
        RAISE NOTICE '';
        RAISE NOTICE 'Views created:';
        RAISE NOTICE '  - active_bookings';
        RAISE NOTICE '  - train_availability_summary';
        RAISE NOTICE '  - user_booking_history';
        RAISE NOTICE '';
        RAISE NOTICE 'Functions created:';
        RAISE NOTICE '  - update_updated_at_column()';
        RAISE NOTICE '  - cleanup_expired_idempotency_keys()';
        RAISE NOTICE '  - cleanup_expired_seat_locks()';
        RAISE NOTICE '  - release_expired_ticket_blocks()';
        RAISE NOTICE '  - generate_booking_id()';
        RAISE NOTICE '';
        RAISE NOTICE 'Schema setup complete! Ready for data insertion.';
    END $$;

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
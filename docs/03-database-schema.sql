-- PostgreSQL 16
CREATE EXTENSION IF NOT EXISTS pgcrypto; CREATE EXTENSION IF NOT EXISTS postgis;
CREATE TYPE visa_type AS ENUM ('free','gcc_id','voa','evisa','eta','required','not_allowed');
CREATE TYPE content_status AS ENUM ('draft','in_review','published','archived');
CREATE TYPE user_role AS ENUM ('user','editor','reviewer','admin');

CREATE TABLE users(
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(), email citext UNIQUE, phone text UNIQUE,
  display_name text, locale text DEFAULT 'ar', role user_role DEFAULT 'user',
  prefs jsonb DEFAULT '{}', created_at timestamptz DEFAULT now(), deleted_at timestamptz);

CREATE TABLE countries(
  iso2 char(2) PRIMARY KEY, name_ar text NOT NULL, name_en text NOT NULL, currency char(3),
  region text, calling_code text);

CREATE TABLE destinations(
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(), slug text UNIQUE NOT NULL,
  country_iso2 char(2) REFERENCES countries, name_ar text NOT NULL, name_en text,
  city_ar text, about_ar text, about_en text, geo geography(Point,4326),
  budget smallint CHECK (budget BETWEEN 1 AND 3), best_seasons text[], tags text[],
  flight_minutes_ruh int, hero_media_id uuid, status content_status DEFAULT 'draft',
  published_at timestamptz, updated_at timestamptz DEFAULT now());
CREATE INDEX ON destinations USING gin(tags); CREATE INDEX ON destinations USING gist(geo);

CREATE TABLE media(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), destination_id uuid REFERENCES destinations ON DELETE CASCADE,
  url text NOT NULL, kind text CHECK (kind IN ('image','video')), alt_ar text NOT NULL, credit text, sort int DEFAULT 0);

-- شروط الدخول لكل (دولة الوجهة × جواز المسافر) مع إصدارات
CREATE TABLE entry_requirements(
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(), destination_country char(2) REFERENCES countries,
  passport_country char(2) REFERENCES countries DEFAULT 'SA', residency_country char(2),
  visa visa_type NOT NULL, max_stay_days int, passport_validity_months int,
  docs jsonb NOT NULL DEFAULT '[]', vaccines jsonb DEFAULT '[]', quarantine_ar text,
  notes_ar text, source text NOT NULL, source_url text, verified_at timestamptz NOT NULL,
  version int NOT NULL DEFAULT 1, status content_status DEFAULT 'draft', provider text, raw_hash text,
  UNIQUE(destination_country, passport_country, residency_country, version));
CREATE TABLE requirement_changes(id bigserial PRIMARY KEY, requirement_id uuid REFERENCES entry_requirements,
  diff jsonb NOT NULL, changed_by uuid REFERENCES users, changed_at timestamptz DEFAULT now());

CREATE TABLE embassies(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), country_iso2 char(2) REFERENCES countries,
  kind text CHECK (kind IN ('embassy_in_ksa','saudi_embassy_abroad','consulate')), name_ar text, phone text, email text, address_ar text, geo geography(Point,4326), hours text);

CREATE TABLE favorites(user_id uuid REFERENCES users ON DELETE CASCADE, destination_id uuid REFERENCES destinations ON DELETE CASCADE, created_at timestamptz DEFAULT now(), PRIMARY KEY(user_id,destination_id));
CREATE TABLE trips(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), user_id uuid REFERENCES users ON DELETE CASCADE, destination_id uuid REFERENCES destinations,
  title text, start_date date, end_date date, travelers jsonb DEFAULT '[]', share_token text UNIQUE);
CREATE TABLE trip_items(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), trip_id uuid REFERENCES trips ON DELETE CASCADE, day int, position int, title text, place_geo geography(Point,4326), notes text, booking_url text);
CREATE TABLE reviews(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), user_id uuid REFERENCES users, destination_id uuid REFERENCES destinations,
  rating smallint CHECK (rating BETWEEN 1 AND 5), body text CHECK (length(body)<=1000), status text DEFAULT 'pending', created_at timestamptz DEFAULT now(), UNIQUE(user_id,destination_id));
CREATE TABLE alert_subscriptions(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), user_id uuid REFERENCES users ON DELETE CASCADE, destination_country char(2) REFERENCES countries,
  channel text[] DEFAULT '{push,email}', created_at timestamptz DEFAULT now(), UNIQUE(user_id,destination_country));
CREATE TABLE push_endpoints(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), user_id uuid REFERENCES users ON DELETE CASCADE, endpoint text UNIQUE, p256dh text, auth text);
CREATE TABLE affiliate_links(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), destination_id uuid REFERENCES destinations, partner text, kind text, url_template text, active bool DEFAULT true);
CREATE TABLE affiliate_clicks(id bigserial PRIMARY KEY, link_id uuid REFERENCES affiliate_links, user_id uuid, at timestamptz DEFAULT now());
CREATE TABLE fx_rates(base char(3), quote char(3), rate numeric(18,8), fetched_at timestamptz, PRIMARY KEY(base,quote));
CREATE TABLE audit_log(id bigserial PRIMARY KEY, actor uuid, action text, entity text, entity_id text, meta jsonb, at timestamptz DEFAULT now());

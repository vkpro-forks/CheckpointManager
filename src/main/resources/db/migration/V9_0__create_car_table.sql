CREATE TABLE cars (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      license_plate VARCHAR(255) NOT NULL,
                      brand_model VARCHAR(255) NOT NULL,
                      type VARCHAR(255) NOT NULL,
                      color VARCHAR(255) NOT NULL,
                      year INTEGER
);
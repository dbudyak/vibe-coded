-- Initial database schema for leaderboard service
-- This runs automatically when the container starts for the first time

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Games table
CREATE TABLE IF NOT EXISTS games (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    api_key VARCHAR(64) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Players table
CREATE TABLE IF NOT EXISTS players (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    player_id VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT unique_player_per_game UNIQUE(game_id, player_id)
);

-- Scores table
CREATE TABLE IF NOT EXISTS scores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    score BIGINT NOT NULL,
    submitted_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_scores_game_score ON scores(game_id, score DESC);
CREATE INDEX IF NOT EXISTS idx_scores_player_score ON scores(player_id, score DESC);
CREATE INDEX IF NOT EXISTS idx_scores_submitted_at ON scores(submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_scores_game_submitted ON scores(game_id, submitted_at DESC);
CREATE INDEX IF NOT EXISTS idx_players_game ON players(game_id);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to games table
CREATE TRIGGER update_games_updated_at BEFORE UPDATE ON games
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to players table
CREATE TRIGGER update_players_updated_at BEFORE UPDATE ON players
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert a demo game for testing (optional - remove in production)
INSERT INTO games (name, api_key, description) 
VALUES (
    'Demo Game',
    'demo_api_key_12345678901234567890123456789012',
    'A demo game for testing the leaderboard service'
) ON CONFLICT (api_key) DO NOTHING;

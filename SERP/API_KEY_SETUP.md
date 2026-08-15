# API Key Setup Guide

## Quick Setup (Interactive)

Run the setup script:
```bash
./setup-env.sh
```

This will:
1. Prompt you to enter your API key securely
2. Set it for the current session
3. Optionally save it permanently to ~/.bashrc

---

## Manual Setup Options

### Option 1: Single Session (Temporary)

Set the key for just this terminal session:
```bash
export S2_API_KEY=your_actual_key_here
```

Then run the application:
```bash
mvn clean compile exec:java
```

### Option 2: Single Run (One-time)

Set the key just for one command:
```bash
S2_API_KEY=your_key_here mvn clean compile exec:java
```

### Option 3: Permanent Setup (Recommended)

Add to your shell configuration file:

**For Bash (`~/.bashrc`):**
```bash
echo 'export S2_API_KEY=your_actual_key_here' >> ~/.bashrc
source ~/.bashrc
```

**For Zsh (`~/.zshrc`):**
```bash
echo 'export S2_API_KEY=your_actual_key_here' >> ~/.zshrc
source ~/.zshrc
```

### Option 4: Project .env File

1. Copy the example file:
```bash
cp .env.example .env
```

2. Edit `.env` and add your key:
```bash
nano .env
```

3. Load and run:
```bash
source .env && mvn clean compile exec:java
```

---

## Verify Setup

Check if your key is set:
```bash
echo ${S2_API_KEY:+Key is SET}${S2_API_KEY:-Key is NOT SET}
```

---

## Running the Application

### With Real API (uses your key):
```bash
mvn clean compile exec:java
```

### With Mock Data (no API key needed):
```bash
USE_MOCK_DATA=true mvn clean compile exec:java
```

### With Both:
```bash
S2_API_KEY=your_key USE_MOCK_DATA=false mvn clean compile exec:java
```

---

## Getting Your API Key

1. Visit: https://www.semanticscholar.org/product/api
2. Sign up or log in
3. Navigate to API Keys section
4. Create a new key or copy existing one

---

## API Key Benefits

- **Higher daily quota**: Thousands of requests per day (vs ~100 anonymous)
- **Priority access**: Your requests are prioritized
- **Request tracking**: Monitor your usage
- **Better reliability**: Less likely to hit limits

**Note:** Rate limit is still 1 request/second for all users (with or without key)

---

## Security Notes

- Never commit your API key to version control
- `.env` is already in `.gitignore` for safety
- The setup script hides your key input
- Keys in ~/.bashrc are only accessible to your user account

---

## Troubleshooting

**Key not working?**
```bash
# Check if it's set
echo $S2_API_KEY

# Check length (should be non-zero)
echo ${#S2_API_KEY}

# Re-export it
export S2_API_KEY=your_key_here
```

**Still getting rate limited?**
- The 1 req/sec limit applies to everyone
- Code is already optimized for this
- Use `USE_MOCK_DATA=true` for testing without API calls

**API key not persisting?**
- Make sure you added it to the correct shell config file
- Run `source ~/.bashrc` (or `~/.zshrc`) to reload
- Check the file: `cat ~/.bashrc | grep S2_API_KEY`

---

## Files Created

- `setup-env.sh` - Interactive setup script
- `.env.example` - Template for environment variables
- `.env` - Your actual environment file (create from example)
- `API_KEY_SETUP.md` - This documentation

---

## Need Help?

The application works perfectly with `USE_MOCK_DATA=true` for development and testing. You only need the API key for fetching real papers from Semantic Scholar.

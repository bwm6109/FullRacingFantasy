import { useState } from 'react';
import axios from 'axios';

export default function ScraperDashboard() {
    // Meet scrape state
    const [url, setUrl] = useState('');
    const [weekNumber, setWeekNumber] = useState(1);

    // Roster import state
    const [gender, setGender] = useState('mens');
    const [rosterUrlsText, setRosterUrlsText] = useState('');

    // Shared UI state
    const [status, setStatus] = useState('idle');
    const [message, setMessage] = useState('');

    const handleScrapeMeet = async (e) => {
        e.preventDefault();

        if (!url) {
            setMessage('Please enter a valid TFRRS meet URL.');
            setStatus('error');
            return;
        }

        setStatus('loading');
        setMessage('Scraping meet data...');

        try {
            const response = await axios.post('http://localhost:8080/api/performances/scrape', {
                url,
                weekNumber: parseInt(weekNumber)
            });

            setStatus('success');
            setMessage(`✅ ${response.data}`);
            setUrl('');
        } catch (error) {
            setStatus('error');
            const errorMsg = error.response?.data || 'Could not connect to the server.';
            setMessage(`❌ Error: ${errorMsg}`);
        }
    };

    const handleImportRosters = async (e) => {
        e.preventDefault();

        const rosterUrls = rosterUrlsText
            .split('\n')
            .map((line) => line.trim())
            .filter((line) => line.length > 0);

        if (rosterUrls.length === 0) {
            setMessage('Please enter at least one team roster URL.');
            setStatus('error');
            return;
        }

        setStatus('loading');
        setMessage('Importing roster data and calculating best events...');

        try {
            const response = await axios.post('http://localhost:8080/api/athletes/import-rosters', {
                gender,
                rosterUrls
            });

            setStatus('success');
            setMessage(`✅ ${response.data}`);
            setRosterUrlsText('');
        } catch (error) {
            setStatus('error');
            const errorMsg = error.response?.data || 'Could not connect to the server.';
            setMessage(`❌ Error: ${errorMsg}`);
        }
    };

    return (
        <div className="scraper-card">
            <h2>Data Scraper Control Center</h2>

            <div className="scraper-sections">
                <div className="scraper-subcard">
                    <h3>Import Team Rosters</h3>
                    <p>Paste multiple TFRRS roster URLs, one per line, to create the draft pool.</p>

                    <form onSubmit={handleImportRosters} className="scraper-form">
                        <div className="input-group">
                            <label>Gender:</label>
                            <select value={gender} onChange={(e) => setGender(e.target.value)}>
                                <option value="mens">Men</option>
                                <option value="womens">Women</option>
                            </select>
                        </div>

                        <div className="input-group">
                            <label>Team Roster URLs:</label>
                            <textarea
                                rows="8"
                                placeholder={`https://www.tfrrs.org/teams/tf/NY_college_m_RIT.html
https://www.tfrrs.org/teams/tf/NY_college_m_St_Lawrence.html
https://www.tfrrs.org/teams/tf/NY_college_m_Ithaca.html`}
                                value={rosterUrlsText}
                                onChange={(e) => setRosterUrlsText(e.target.value)}
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={status === 'loading'}
                            className={status === 'loading' ? 'loading-btn' : 'submit-btn'}
                        >
                            {status === 'loading' ? 'Importing...' : 'Import Rosters'}
                        </button>
                    </form>
                </div>

                <div className="scraper-subcard">
                    <h3>Scrape Meet Results</h3>
                    <p>Enter a TFRRS meet URL to import performances for a fantasy week.</p>

                    <form onSubmit={handleScrapeMeet} className="scraper-form">
                        <div className="input-group">
                            <label>TFRRS Meet URL:</label>
                            <input
                                type="url"
                                placeholder="https://www.tfrrs.org/results/..."
                                value={url}
                                onChange={(e) => setUrl(e.target.value)}
                                required
                            />
                        </div>

                        <div className="input-group">
                            <label>Fantasy Week Number:</label>
                            <input
                                type="number"
                                min="1"
                                max="20"
                                value={weekNumber}
                                onChange={(e) => setWeekNumber(e.target.value)}
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={status === 'loading'}
                            className={status === 'loading' ? 'loading-btn' : 'submit-btn'}
                        >
                            {status === 'loading' ? 'Scraping...' : 'Scrape Meet'}
                        </button>
                    </form>
                </div>
            </div>

            {message && (
                <div className={`message-box ${status}`}>
                    {message}
                </div>
            )}
        </div>
    );
}
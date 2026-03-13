import { useState } from 'react';
import axios from 'axios';

export default function ScraperDashboard() {
    // 1. Set up state for our form inputs and UI feedback
    const [url, setUrl] = useState('');
    const [weekNumber, setWeekNumber] = useState(1);
    const [status, setStatus] = useState('idle'); // 'idle', 'loading', 'success', 'error'
    const [message, setMessage] = useState('');

    // 2. The function that triggers when you click "Scrape Meet"
    const handleScrape = async (e) => {
        e.preventDefault(); // Prevents the page from refreshing

        if (!url) {
            setMessage('Please enter a valid TFRRS URL.');
            setStatus('error');
            return;
        }

        setStatus('loading');
        setMessage('Scraping meet data... This might take a few seconds.');

        try {
            // 3. Send the POST request to your Spring Boot backend
            const response = await axios.post('http://localhost:8080/api/performances/scrape', {
                url: url,
                weekNumber: parseInt(weekNumber)
            });

            setStatus('success');
            setMessage(`✅ ${response.data}`);
            setUrl(''); // Clear the input field after success

        } catch (error) {
            setStatus('error');
            // Extract the error message from Spring Boot if it exists
            const errorMsg = error.response?.data || 'Could not connect to the server.';
            setMessage(`❌ Error: ${errorMsg}`);
        }
    };

    return (
        <div className="scraper-card">
            <h2>⚙️ Data Scraper Control Center</h2>
            <p>Enter a TFRRS meet URL to import athletes and performances into the database.</p>

            <form onSubmit={handleScrape} className="scraper-form">
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

            {/* Conditional rendering to show feedback messages */}
            {message && (
                <div className={`message-box ${status}`}>
                    {message}
                </div>
            )}
        </div>
    );
}
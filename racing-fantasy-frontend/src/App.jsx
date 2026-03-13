import ScraperDashboard from './components/ScraperDashboard';
import DraftRoom from './components/DraftRoom';
import './App.css';

function App() {
    return (
        <div className="App">
            <header className="app-header">
                <h1>🏆 Track & Field Fantasy</h1>
            </header>

            <main className="app-content">
                <ScraperDashboard />
                <div style={{ marginTop: '40px' }}></div> {/* Spacer */}
                <DraftRoom />
            </main>
        </div>
    );
}

export default App;
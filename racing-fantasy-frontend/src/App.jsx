import ScraperDashboard from './components/ScraperDashboard';
import DraftRoom from './components/DraftRoom';
import TeamRoster from './components/TeamRoster';
import LeagueManager from './components/LeagueManager';
import UserTeamManager from './components/UserTeamManager';
import './App.css';

function App() {
    return (
        <div className="App">
            <header className="app-header">
                <h1>Fantasy Track & Field</h1>
            </header>

            <main className="app-content">
                <UserTeamManager />
                <div style={{ marginTop: '40px' }}></div>

                <ScraperDashboard />
                <div style={{ marginTop: '40px' }}></div>

                <DraftRoom />
                <div style={{ marginTop: '40px' }}></div>

                <TeamRoster />
                <div style={{ marginTop: '40px' }}></div>

                <LeagueManager />
            </main>
        </div>
    );
}

export default App;
import { useState, useEffect } from 'react';
import axios from 'axios';

export default function LeagueManager() {
    const [leagues, setLeagues] = useState([]);
    const [teams, setTeams] = useState([]);
    const [message, setMessage] = useState('');

    // Create League Form States
    const [newLeagueName, setNewLeagueName] = useState('');
    const [newJoinCode, setNewJoinCode] = useState('');
    const [newDivision, setNewDivision] = useState('D3');
    const [newConference, setNewConference] = useState('Liberty League');
    const [newGender, setNewGender] = useState('mens');

    // Join League Form States
    const [joinLeagueId, setJoinLeagueId] = useState('');
    const [joinTeamId, setJoinTeamId] = useState('');
    const [joinPasscode, setJoinPasscode] = useState('');

    // Leaderboard States
    const [viewLeagueId, setViewLeagueId] = useState('');
    const [leaderboardWeek, setLeaderboardWeek] = useState(1);
    const [leaderboard, setLeaderboard] = useState([]);

    // Fetch Leagues and Teams on load
    const fetchData = async () => {
        try {
            const [leaguesRes, teamsRes] = await Promise.all([
                axios.get('http://localhost:8080/api/leagues'),
                axios.get('http://localhost:8080/api/teams')
            ]);

            setLeagues(leaguesRes.data);
            setTeams(teamsRes.data);

            if (leaguesRes.data.length > 0) {
                setJoinLeagueId(leaguesRes.data[0].id);
                setViewLeagueId(leaguesRes.data[0].id);
            } else {
                setJoinLeagueId('');
                setViewLeagueId('');
            }

            if (teamsRes.data.length > 0) {
                setJoinTeamId(teamsRes.data[0].id);
            } else {
                setJoinTeamId('');
            }
        } catch (error) {
            console.error("Error fetching data:", error);
            setMessage("❌ Failed to load leagues or teams.");
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    // Handle Creating a League
    const handleCreateLeague = async (e) => {
        e.preventDefault();

        try {
            await axios.post('http://localhost:8080/api/leagues', {
                leagueName: newLeagueName,
                joinCode: newJoinCode,
                division: newDivision,
                conference: newConference,
                gender: newGender
            });

            setMessage(`✅ Successfully created league: ${newLeagueName}`);
            setNewLeagueName('');
            setNewJoinCode('');
            setNewDivision('D3');
            setNewConference('Liberty League');
            setNewGender('mens');

            fetchData();
        } catch (error) {
            setMessage(`❌ Error creating league: ${error.response?.data || error.message}`);
        }
    };

    // Handle Joining a League
    const handleJoinLeague = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.post(
                `http://localhost:8080/api/leagues/${joinLeagueId}/join?teamId=${joinTeamId}&joinCode=${joinPasscode}`
            );
            setMessage(`✅ ${response.data}`);
            setJoinPasscode('');
        } catch (error) {
            setMessage(`❌ ${error.response?.data || "Could not join league."}`);
        }
    };

    // Handle Fetching Leaderboard
    const fetchLeaderboard = async () => {
        if (!viewLeagueId) {
            setLeaderboard([]);
            return;
        }

        try {
            const response = await axios.get(
                `http://localhost:8080/api/leagues/${viewLeagueId}/leaderboard?week=${leaderboardWeek}`
            );
            setLeaderboard(response.data);
            setMessage('');
        } catch (error) {
            setLeaderboard([]);
            setMessage("❌ Error fetching leaderboard.");
        }
    };

    useEffect(() => {
        fetchLeaderboard();
    }, [viewLeagueId, leaderboardWeek]);

    return (
        <div className="league-card">
            <h2>🌍 League Hub</h2>

            {message && <div className="message-box">{message}</div>}

            <div className="league-grid">
                {/* CREATE LEAGUE SECTION */}
                <div className="league-section">
                    <h3>Create a League</h3>
                    <form onSubmit={handleCreateLeague} className="league-form">
                        <input
                            type="text"
                            placeholder="League Name (e.g. D3 Liberty Legends)"
                            value={newLeagueName}
                            onChange={(e) => setNewLeagueName(e.target.value)}
                            required
                        />

                        <input
                            type="text"
                            placeholder="Secret Join Code"
                            value={newJoinCode}
                            onChange={(e) => setNewJoinCode(e.target.value)}
                            required
                        />

                        <select
                            value={newDivision}
                            onChange={(e) => setNewDivision(e.target.value)}
                            required
                        >
                            <option value="D1">Division 1</option>
                            <option value="D2">Division 2</option>
                            <option value="D3">Division 3</option>
                            <option value="NAIA">NAIA</option>
                            <option value="NJCAA">NJCAA</option>
                        </select>

                        <input
                            type="text"
                            placeholder="Conference (e.g. Liberty League)"
                            value={newConference}
                            onChange={(e) => setNewConference(e.target.value)}
                            required
                        />

                        <select
                            value={newGender}
                            onChange={(e) => setNewGender(e.target.value)}
                            required
                        >
                            <option value="mens">Men</option>
                            <option value="womens">Women</option>
                        </select>

                        <button type="submit" className="action-btn">
                            Create League
                        </button>
                    </form>
                </div>

                {/* JOIN LEAGUE SECTION */}
                <div className="league-section">
                    <h3>Join a League</h3>
                    <form onSubmit={handleJoinLeague} className="league-form">
                        <select
                            value={joinTeamId}
                            onChange={(e) => setJoinTeamId(e.target.value)}
                            required
                        >
                            <option value="" disabled>Select your team...</option>
                            {teams.map((team) => (
                                <option key={team.id} value={team.id}>
                                    {team.teamName}
                                </option>
                            ))}
                        </select>

                        <select
                            value={joinLeagueId}
                            onChange={(e) => setJoinLeagueId(e.target.value)}
                            required
                        >
                            <option value="" disabled>Select a league...</option>
                            {leagues.map((league) => (
                                <option key={league.id} value={league.id}>
                                    {league.leagueName}
                                </option>
                            ))}
                        </select>

                        <input
                            type="text"
                            placeholder="Enter Join Code"
                            value={joinPasscode}
                            onChange={(e) => setJoinPasscode(e.target.value)}
                            required
                        />

                        <button type="submit" className="action-btn join-btn">
                            Join League
                        </button>
                    </form>
                </div>
            </div>

            <hr className="divider" />

            {/* LEADERBOARD SECTION */}
            <div className="leaderboard-section">
                <h3>🏆 League Leaderboard</h3>

                <div className="leaderboard-controls">
                    <select
                        value={viewLeagueId}
                        onChange={(e) => setViewLeagueId(e.target.value)}
                    >
                        {leagues.length === 0 && <option value="">No leagues available...</option>}
                        {leagues.map((league) => (
                            <option key={league.id} value={league.id}>
                                {league.leagueName}
                            </option>
                        ))}
                    </select>

                    <input
                        type="number"
                        min="1"
                        max="20"
                        value={leaderboardWeek}
                        onChange={(e) => setLeaderboardWeek(e.target.value)}
                        title="Week Number"
                    />

                    <button onClick={fetchLeaderboard} className="action-btn refresh-btn">
                        Refresh
                    </button>
                </div>

                {viewLeagueId && (
                    <div className="league-meta" style={{ marginBottom: '16px', textAlign: 'left' }}>
                        {(() => {
                            const selectedLeague = leagues.find(
                                (league) => String(league.id) === String(viewLeagueId)
                            );

                            if (!selectedLeague) return null;

                            return (
                                <>
                                    <p><strong>Division:</strong> {selectedLeague.division || 'Not set'}</p>
                                    <p><strong>Conference:</strong> {selectedLeague.conference || 'Not set'}</p>
                                    <p><strong>Gender:</strong> {selectedLeague.gender || 'Not set'}</p>
                                </>
                            );
                        })()}
                    </div>
                )}

                <table className="leaderboard-table">
                    <thead>
                    <tr>
                        <th>Rank</th>
                        <th>Team Name</th>
                        <th>Owner</th>
                        <th>Points</th>
                    </tr>
                    </thead>
                    <tbody>
                    {leaderboard.length === 0 ? (
                        <tr>
                            <td colSpan="4">No teams found in this league.</td>
                        </tr>
                    ) : (
                        leaderboard.map((team, index) => (
                            <tr key={index}>
                                <td className="rank-cell">#{index + 1}</td>
                                <td><strong>{team.teamName}</strong></td>
                                <td>{team.ownerName}</td>
                                <td className="score-cell">{team.score.toFixed(2)}</td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
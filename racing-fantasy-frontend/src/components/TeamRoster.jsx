import { useState, useEffect } from 'react';
import axios from 'axios';

export default function TeamRoster() {
    const [teams, setTeams] = useState([]);
    const [selectedTeamId, setSelectedTeamId] = useState('');
    const [teamData, setTeamData] = useState(null);
    const [teamScore, setTeamScore] = useState(0);
    const [weekNumber, setWeekNumber] = useState(1);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);

    // 1. Fetch the list of teams when the component loads
    useEffect(() => {
        const fetchTeams = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/teams');
                setTeams(response.data);
                if (response.data.length > 0) {
                    setSelectedTeamId(response.data[0].id);
                }
            } catch (error) {
                console.error("Error fetching teams:", error);
            }
        };
        fetchTeams();
    }, []);

    // 2. Fetch specific team roster and score whenever the team or week changes
    useEffect(() => {
        if (!selectedTeamId) return;

        const fetchTeamDetails = async () => {
            setLoading(true);
            setMessage('');
            try {
                // Run both API calls at the same time for speed
                const [rosterRes, scoreRes] = await Promise.all([
                    axios.get(`http://localhost:8080/api/teams/${selectedTeamId}`),
                    axios.get(`http://localhost:8080/api/teams/${selectedTeamId}/score?week=${weekNumber}`)
                ]);

                setTeamData(rosterRes.data);
                setTeamScore(scoreRes.data);
            } catch (error) {
                console.error("Error fetching team details:", error);
                setMessage("❌ Failed to load team data.");
            } finally {
                setLoading(false);
            }
        };

        fetchTeamDetails();
    }, [selectedTeamId, weekNumber]);

    // 3. Handle dropping an athlete
    const handleDrop = async (athleteId, athleteName) => {
        try {
            await axios.delete(`http://localhost:8080/api/teams/${selectedTeamId}/roster/${athleteId}`);
            setMessage(`✅ Dropped ${athleteName} from the roster.`);

            // Remove the athlete from the UI immediately without reloading the page
            setTeamData(prevData => ({
                ...prevData,
                roster: prevData.roster.filter(athlete => athlete.id !== athleteId)
            }));
        } catch (error) {
            setMessage(`❌ Error dropping athlete: ${error.response?.data || "Unknown error"}`);
        }
    };

    return (
        <div className="roster-card">
            <h2>Team Roster & Scores</h2>

            <div className="roster-controls">
                <div className="input-group">
                    <label>Select Team:</label>
                    <select
                        value={selectedTeamId}
                        onChange={(e) => setSelectedTeamId(e.target.value)}
                    >
                        {teams.length === 0 && <option value="">No teams available...</option>}
                        {teams.map(team => (
                            <option key={team.id} value={team.id}>
                                {team.teamName}
                            </option>
                        ))}
                    </select>
                </div>

                <div className="input-group">
                    <label>Week Number:</label>
                    <input
                        type="number"
                        min="1"
                        max="20"
                        value={weekNumber}
                        onChange={(e) => setWeekNumber(e.target.value)}
                    />
                </div>
            </div>

            {message && <div className="message-box">{message}</div>}

            {loading ? (
                <p>Loading roster...</p>
            ) : teamData ? (
                <>
                    <div className="scoreboard">
                        <h3>Total Points (Week {weekNumber}): <span className="highlight-score">{teamScore}</span></h3>
                    </div>

                    <div className="athlete-grid">
                        {(!teamData.roster || teamData.roster.length === 0) ? (
                            <p>Your roster is empty. Go draft some athletes!</p>
                        ) : (
                            teamData.roster.map(athlete => (
                                <div key={athlete.id} className="athlete-card roster-view">
                                    <div className="athlete-info">
                                        <strong>{athlete.name}</strong>
                                        <span>{athlete.school}</span>
                                    </div>
                                    <button
                                        onClick={() => handleDrop(athlete.id, athlete.name)}
                                        className="drop-btn"
                                    >
                                        Drop
                                    </button>
                                </div>
                            ))
                        )}
                    </div>
                </>
            ) : null}
        </div>
    );
}
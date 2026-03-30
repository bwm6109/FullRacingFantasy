import { useState, useEffect } from 'react';
import axios from 'axios';

export default function TeamRoster() {
    const [teams, setTeams] = useState([]);
    const [selectedTeamId, setSelectedTeamId] = useState('');
    const [teamData, setTeamData] = useState(null);
    const [teamScore, setTeamScore] = useState(0);
    const [athleteScores, setAthleteScores] = useState({});
    const [weekNumber, setWeekNumber] = useState(1);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);

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

    useEffect(() => {
        if (!selectedTeamId) return;

        const fetchTeamDetails = async () => {
            setLoading(true);
            setMessage('');

            try {
                const [rosterRes, scoreRes] = await Promise.all([
                    axios.get(`http://localhost:8080/api/teams/${selectedTeamId}`),
                    axios.get(`http://localhost:8080/api/teams/${selectedTeamId}/score?week=${weekNumber}`)
                ]);

                const rosterData = rosterRes.data;
                setTeamData(rosterData);
                setTeamScore(scoreRes.data);

                if (rosterData.roster && rosterData.roster.length > 0) {
                    const scoreRequests = rosterData.roster.map(async (athlete) => {
                        try {
                            const res = await axios.get(
                                `http://localhost:8080/api/athletes/${athlete.id}/score?week=${weekNumber}`
                            );
                            return { athleteId: athlete.id, score: res.data };
                        } catch (error) {
                            console.error(`Error fetching score for ${athlete.name}:`, error);
                            return { athleteId: athlete.id, score: 0 };
                        }
                    });

                    const scoreResults = await Promise.all(scoreRequests);

                    const scoreMap = {};
                    scoreResults.forEach(({ athleteId, score }) => {
                        scoreMap[athleteId] = score;
                    });

                    setAthleteScores(scoreMap);
                } else {
                    setAthleteScores({});
                }
            } catch (error) {
                console.error("Error fetching team details:", error);
                setMessage("❌ Failed to load team data.");
            } finally {
                setLoading(false);
            }
        };

        fetchTeamDetails();
    }, [selectedTeamId, weekNumber]);

    const handleDrop = async (athleteId, athleteName) => {
        try {
            await axios.delete(`http://localhost:8080/api/teams/${selectedTeamId}/roster/${athleteId}`);
            setMessage(`✅ Dropped ${athleteName} from the roster.`);

            setTeamData(prevData => ({
                ...prevData,
                roster: prevData.roster.filter(athlete => athlete.id !== athleteId)
            }));

            setAthleteScores(prevScores => {
                const updatedScores = { ...prevScores };
                delete updatedScores[athleteId];
                return updatedScores;
            });
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
                        onChange={(e) => setWeekNumber(Number(e.target.value))}
                    />
                </div>
            </div>

            {message && <div className="message-box">{message}</div>}

            {loading ? (
                <p>Loading roster...</p>
            ) : teamData ? (
                <>
                    <div className="scoreboard">
                        <h3>
                            Total Points (Week {weekNumber}):{' '}
                            <span className="highlight-score">{teamScore}</span>
                        </h3>
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
                                        <span className="athlete-week-score">
                                            Week {weekNumber} Points: {athleteScores[athlete.id] ?? 0}
                                        </span>
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
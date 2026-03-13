import { useState, useEffect } from 'react';
import axios from 'axios';

export default function DraftRoom() {
    const [teams, setTeams] = useState([]);
    const [athletes, setAthletes] = useState([]);
    const [selectedTeamId, setSelectedTeamId] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(true);

    // Fetch Teams and Athletes when the component loads
    useEffect(() => {
        const fetchData = async () => {
            try {
                const [teamsRes, athletesRes] = await Promise.all([
                    axios.get('http://localhost:8080/api/teams'),
                    axios.get('http://localhost:8080/api/athletes')
                ]);
                setTeams(teamsRes.data);
                setAthletes(athletesRes.data);

                // Auto-select the first team if available
                if (teamsRes.data.length > 0) {
                    setSelectedTeamId(teamsRes.data[0].id);
                }
            } catch (error) {
                console.error("Error fetching data:", error);
                setMessage("❌ Failed to load teams or athletes.");
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    // Handle the Draft button click
    const handleDraft = async (athleteId, athleteName) => {
        if (!selectedTeamId) {
            setMessage("⚠️ Please select a team first!");
            return;
        }

        try {
            const response = await axios.post(`http://localhost:8080/api/teams/${selectedTeamId}/draft/${athleteId}`);
            setMessage(`✅ ${response.data}`);
        } catch (error) {
            const errorMsg = error.response?.data || "Could not draft athlete.";
            setMessage(`❌ ${errorMsg}`);
        }
    };

    // Filter athletes based on the search bar
    const filteredAthletes = athletes.filter(athlete =>
        athlete.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        athlete.school.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (loading) return <p>Loading Draft Room...</p>;

    return (
        <div className="draft-room-card">
            <h2>🎯 Draft Room</h2>

            <div className="draft-controls">
                <div className="input-group">
                    <label>Select Team to Draft For:</label>
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
                    <label>Search Athletes (Name or School):</label>
                    <input
                        type="text"
                        placeholder="e.g., Noah Lyles or Florida..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
            </div>

            {message && <div className="message-box">{message}</div>}

            <div className="athlete-grid">
                {filteredAthletes.length === 0 ? (
                    <p>No athletes found. Try scraping a meet first!</p>
                ) : (
                    filteredAthletes.slice(0, 50).map(athlete => ( // Limiting to 50 so the browser doesn't lag
                        <div key={athlete.id} className="athlete-card">
                            <div className="athlete-info">
                                <strong>{athlete.name}</strong>
                                <span>{athlete.school}</span>
                            </div>
                            <button onClick={() => handleDraft(athlete.id, athlete.name)} className="draft-btn">
                                Draft
                            </button>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}
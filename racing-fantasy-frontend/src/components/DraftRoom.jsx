import { useState, useEffect, useMemo } from 'react';
import axios from 'axios';

export default function DraftRoom() {
    const [teams, setTeams] = useState([]);
    const [athletes, setAthletes] = useState([]);
    const [selectedTeamId, setSelectedTeamId] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const [message, setMessage] = useState('');
    const [loadingTeams, setLoadingTeams] = useState(true);
    const [loadingAthletes, setLoadingAthletes] = useState(true);

    const fetchTeams = async () => {
        setLoadingTeams(true);
        try {
            const response = await axios.get('http://localhost:8080/api/teams');
            const teamData = Array.isArray(response.data) ? response.data : [];

            setTeams(teamData);

            if (teamData.length > 0) {
                setSelectedTeamId(String(teamData[0].id));
            } else {
                setSelectedTeamId('');
            }
        } catch (error) {
            console.error('Error fetching teams:', error);
            setTeams([]);
            setMessage('❌ Failed to load teams.');
        } finally {
            setLoadingTeams(false);
        }
    };

    const fetchAthletes = async () => {
        setLoadingAthletes(true);
        try {
            const response = await axios.get('http://localhost:8080/api/athletes');
            const athleteData = Array.isArray(response.data) ? response.data : [];
            setAthletes(athleteData);
        } catch (error) {
            console.error('Error fetching athletes:', error);
            setAthletes([]);
            setMessage('❌ Failed to load athletes.');
        } finally {
            setLoadingAthletes(false);
        }
    };

    const fetchData = async () => {
        setMessage('');
        await Promise.allSettled([fetchTeams(), fetchAthletes()]);
    };

    useEffect(() => {
        fetchData();
    }, []);

    const selectedTeam = useMemo(() => {
        return teams.find((team) => String(team.id) === String(selectedTeamId)) || null;
    }, [teams, selectedTeamId]);

    const selectedLeagueId = selectedTeam?.league?.id ?? null;

    const takenAthletesInLeague = useMemo(() => {
        const takenMap = new Map();

        if (!selectedLeagueId) {
            return takenMap;
        }

        teams.forEach((team) => {
            if (String(team?.league?.id) !== String(selectedLeagueId)) {
                return;
            }

            const roster = Array.isArray(team.roster) ? team.roster : [];
            roster.forEach((athlete) => {
                if (athlete?.id != null) {
                    takenMap.set(athlete.id, {
                        teamId: team.id,
                        teamName: team.teamName,
                        ownerName: team.owner?.username || 'Unknown Owner'
                    });
                }
            });
        });

        return takenMap;
    }, [teams, selectedLeagueId]);

    const handleDraft = async (athleteId, athleteName) => {
        if (!selectedTeamId) {
            setMessage('⚠️ Please create or select a team first.');
            return;
        }

        const takenInfo = takenAthletesInLeague.get(athleteId);
        if (takenInfo && String(takenInfo.teamId) !== String(selectedTeamId)) {
            setMessage(`❌ ${athleteName} is already rostered by ${takenInfo.teamName}.`);
            return;
        }

        try {
            const response = await axios.post(
                `http://localhost:8080/api/teams/${selectedTeamId}/draft/${athleteId}`
            );
            setMessage(`✅ ${response.data}`);

            await fetchTeams();
        } catch (error) {
            const errorMsg = error.response?.data || 'Could not draft athlete.';
            setMessage(`❌ ${errorMsg}`);
        }
    };

    const filteredAthletes = useMemo(() => {
        const term = searchTerm.toLowerCase();

        return athletes
            .filter((athlete) => {
                const name = athlete?.name?.toLowerCase() || '';
                const school = athlete?.school?.toLowerCase() || '';
                const bestEvent = athlete?.bestEvent?.toLowerCase() || '';

                return (
                    name.includes(term) ||
                    school.includes(term) ||
                    bestEvent.includes(term)
                );
            })
            .sort((a, b) => {
                const aValue = a?.bestEventFantasyPoints ?? -1;
                const bValue = b?.bestEventFantasyPoints ?? -1;

                if (bValue !== aValue) {
                    return bValue - aValue;
                }

                const aName = a?.name || '';
                const bName = b?.name || '';
                return aName.localeCompare(bName);
            });
    }, [athletes, searchTerm]);

    const isLoading = loadingTeams || loadingAthletes;

    return (
        <div className="draft-room-card">
            <h2>Draft Room</h2>

            <div className="draft-summary">
                <p><strong>Teams loaded:</strong> {teams.length}</p>
                <p><strong>Athletes loaded:</strong> {athletes.length}</p>
                <p>
                    <strong>Selected League:</strong>{' '}
                    {selectedTeam?.league?.leagueName || 'No league selected'}
                </p>
            </div>

            <div className="draft-controls">
                <div className="input-group">
                    <label>Select Team to Draft For:</label>
                    <select
                        value={selectedTeamId}
                        onChange={(e) => setSelectedTeamId(e.target.value)}
                    >
                        {teams.length === 0 ? (
                            <option value="">No teams available...</option>
                        ) : (
                            teams.map((team) => (
                                <option key={team.id} value={String(team.id)}>
                                    {team.teamName}
                                    {team.owner ? ` (${team.owner.username})` : ''}
                                    {team.league ? ` - ${team.league.leagueName}` : ''}
                                </option>
                            ))
                        )}
                    </select>
                </div>

                <div className="input-group">
                    <label>Search Athletes (Name, School, or Best Event):</label>
                    <input
                        type="text"
                        placeholder="e.g., Hocker, RIT, 1500, pole vault..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                <div className="input-group">
                    <label>&nbsp;</label>
                    <button onClick={fetchData} className="action-btn">
                        Refresh Draft Pool
                    </button>
                </div>
            </div>

            {message && <div className="message-box">{message}</div>}

            {isLoading ? (
                <p>Loading Draft Room...</p>
            ) : (
                <div className="athlete-grid">
                    {filteredAthletes.length === 0 ? (
                        <p>
                            No athletes found.
                            {athletes.length === 0
                                ? ' Try importing roster data first.'
                                : ' Try a different search.'}
                        </p>
                    ) : (
                        filteredAthletes.slice(0, 400).map((athlete) => {
                            const takenInfo = takenAthletesInLeague.get(athlete.id);
                            const isTakenByOtherTeam =
                                takenInfo &&
                                String(takenInfo.teamId) !== String(selectedTeamId);

                            const isOnSelectedTeam =
                                takenInfo &&
                                String(takenInfo.teamId) === String(selectedTeamId);

                            return (
                                <div
                                    key={athlete.id}
                                    className={`athlete-card draft-expanded-card ${
                                        isTakenByOtherTeam ? 'athlete-taken-card' : ''
                                    }`}
                                >
                                    <div className="athlete-info">
                                        <strong>{athlete.name || 'Unknown Athlete'}</strong>
                                        <span>{athlete.school || 'Unknown School'}</span>

                                        <div className="athlete-extra">
                                            <span>
                                                <strong>Best Event:</strong>{' '}
                                                {athlete.bestEvent || 'Not available yet'}
                                            </span>
                                            <span>
                                                <strong>Best Mark:</strong>{' '}
                                                {athlete.bestEventMark || '—'}
                                            </span>
                                            <span>
                                                <strong>Fantasy Value:</strong>{' '}
                                                {athlete.bestEventFantasyPoints != null
                                                    ? Number(athlete.bestEventFantasyPoints).toFixed(2)
                                                    : '—'}
                                            </span>

                                            {isTakenByOtherTeam && (
                                                <span className="taken-label">
                                                    Taken by {takenInfo.teamName}
                                                    {takenInfo.ownerName
                                                        ? ` (${takenInfo.ownerName})`
                                                        : ''}
                                                </span>
                                            )}

                                            {isOnSelectedTeam && (
                                                <span className="owned-label">
                                                    Already on this team
                                                </span>
                                            )}
                                        </div>
                                    </div>

                                    <button
                                        onClick={() => handleDraft(athlete.id, athlete.name)}
                                        className="draft-btn"
                                        disabled={!selectedTeamId || isTakenByOtherTeam || isOnSelectedTeam}
                                    >
                                        {isTakenByOtherTeam
                                            ? 'Taken'
                                            : isOnSelectedTeam
                                                ? 'On Roster'
                                                : 'Draft'}
                                    </button>
                                </div>
                            );
                        })
                    )}
                </div>
            )}
        </div>
    );
}
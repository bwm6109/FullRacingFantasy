import { useState, useEffect } from 'react';
import axios from 'axios';

export default function UserTeamManager() {
    const [users, setUsers] = useState([]);
    const [teams, setTeams] = useState([]);
    const [message, setMessage] = useState('');

    // User form state
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');

    // Team form state
    const [teamName, setTeamName] = useState('');
    const [selectedUserId, setSelectedUserId] = useState('');

    const fetchData = async () => {
        try {
            const [usersRes, teamsRes] = await Promise.all([
                axios.get('http://localhost:8080/api/users'),
                axios.get('http://localhost:8080/api/teams')
            ]);

            setUsers(usersRes.data);
            setTeams(teamsRes.data);

            if (usersRes.data.length > 0) {
                setSelectedUserId(usersRes.data[0].id);
            } else {
                setSelectedUserId('');
            }
        } catch (error) {
            console.error('Error fetching users/teams:', error);
            setMessage('❌ Failed to load users or teams.');
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const handleCreateUser = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.post('http://localhost:8080/api/users', {
                username,
                email
            });

            setMessage(`✅ Created user: ${response.data.username}`);
            setUsername('');
            setEmail('');
            fetchData();
        } catch (error) {
            setMessage(`❌ Error creating user: ${error.response?.data || error.message}`);
        }
    };

    const handleCreateTeam = async (e) => {
        e.preventDefault();

        if (!selectedUserId) {
            setMessage('❌ Please create or select a user first.');
            return;
        }

        try {
            const response = await axios.post(
                `http://localhost:8080/api/teams?userId=${selectedUserId}`,
                { teamName }
            );

            setMessage(`✅ Created team: ${response.data.teamName}`);
            setTeamName('');
            fetchData();
        } catch (error) {
            setMessage(`❌ Error creating team: ${error.response?.data || error.message}`);
        }
    };

    return (
        <div className="user-team-card">
            <h2>User & Team Setup</h2>

            {message && <div className="message-box">{message}</div>}

            <div className="user-team-grid">
                <div className="user-team-section">
                    <h3>Create User</h3>
                    <form onSubmit={handleCreateUser} className="user-team-form">
                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                        {/*<input*/}
                        {/*    type="email"*/}
                        {/*    placeholder="Email"*/}
                        {/*    value={email}*/}
                        {/*    onChange={(e) => setEmail(e.target.value)}*/}
                        {/*    required*/}
                        {/*/>*/}
                        <button type="submit" className="action-btn">
                            Create User
                        </button>
                    </form>
                </div>

                <div className="user-team-section">
                    <h3>Create Team</h3>
                    <form onSubmit={handleCreateTeam} className="user-team-form">
                        <select
                            value={selectedUserId}
                            onChange={(e) => setSelectedUserId(e.target.value)}
                            required
                        >
                            {users.length === 0 && (
                                <option value="">No users available...</option>
                            )}
                            {users.map((user) => (
                                <option key={user.id} value={user.id}>
                                    {user.username}
                                </option>
                            ))}
                        </select>

                        <input
                            type="text"
                            placeholder="Team Name"
                            value={teamName}
                            onChange={(e) => setTeamName(e.target.value)}
                            required
                        />

                        <button type="submit" className="action-btn">
                            Create Team
                        </button>
                    </form>
                </div>
            </div>

            <hr className="divider" />

            <div className="existing-data-section">
                <div className="existing-list">
                    <h3>Existing Users</h3>
                    {users.length === 0 ? (
                        <p>No users created yet.</p>
                    ) : (
                        users.map((user) => (
                            <div key={user.id} className="existing-card">
                                <strong>{user.username}</strong>
                                <span>{user.email}</span>
                            </div>
                        ))
                    )}
                </div>

                <div className="existing-list">
                    <h3>Existing Teams</h3>
                    {teams.length === 0 ? (
                        <p>No teams created yet.</p>
                    ) : (
                        teams.map((team) => (
                            <div key={team.id} className="existing-card">
                                <strong>{team.teamName}</strong>
                                <span>
                                    Owner: {team.owner ? team.owner.username : 'Unknown'}
                                </span>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}
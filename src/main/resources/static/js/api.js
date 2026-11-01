async function request(path, options = {}) {
  const headers = { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers };
  const response = await fetch(path, { ...options, headers });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.detail || data.message || `Request failed (${response.status})`);
  return data;
}

const root = gameId => `/api/games/${encodeURIComponent(gameId)}`;
const post = body => ({ method: "POST", body: JSON.stringify(body) });

export const api = {
  createGame: profile => request("/api/games", post(profile)),
  getCase: gameId => request(`${root(gameId)}/case`),
  getDashboard: gameId => request(`${root(gameId)}/dashboard`),
  getLocations: gameId => request(`${root(gameId)}/locations`),
  investigate: (gameId, locationName) => request(`${root(gameId)}/investigations`, post({ locationName })),
  getSuspects: gameId => request(`${root(gameId)}/suspects`),
  getEvidence: gameId => request(`${root(gameId)}/evidence`),
  getQuestions: (gameId, suspectName) => request(`${root(gameId)}/interrogations/questions?suspectName=${encodeURIComponent(suspectName)}`),
  askQuestion: (gameId, suspectName, questionId) => request(`${root(gameId)}/interrogations`, post({ suspectName, questionId })),
  presentEvidence: (gameId, suspectName, evidenceId) => request(`${root(gameId)}/interrogations/evidence`, post({ suspectName, evidenceId })),
  submitTheory: (gameId, suspectName, evidenceIds) => request(`${root(gameId)}/deductions`, post({ suspectName, evidenceIds })),
  getReport: gameId => request(`${root(gameId)}/report`)
};

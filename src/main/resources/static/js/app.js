import { api } from "./api.js";
import { state, setGameId } from "./state.js";

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
const esc = value => String(value ?? "").replace(/[&<>\"']/g, ch => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;" })[ch]);
const titles = { briefing: "Case briefing", dashboard: "Case overview", suspects: "Persons of interest", locations: "Locations", evidence: "Evidence room", interrogation: "Interrogation", deduction: "Deduction board", report: "Final case report" };
let selectedSuspect = "";
let selectedLocation = "";
let theorySuspect = "";
let linkedEvidence = new Set();
let questionList = [];

const screens = ["dashboard", "suspects", "locations", "evidence", "interrogation", "deduction", "report"];
function showScreen(name) {
  $$(".game-screen").forEach(el => el.hidden = el.id !== `screen-${name}`);
  $$(".nav-item").forEach(el => el.classList.toggle("active", el.dataset.screen === name));
  $("#topScreenTitle").textContent = titles[name] || "Case file";
  if (name === "report") loadReport();
  window.scrollTo({ top: 0, behavior: "smooth" });
}
function showError(message) { const box = $("#registrationError"); if (box) box.textContent = message; }
function updateTop() {
  if (!state.dashboard) return;
  $("#topScore").textContent = `${state.dashboard.score} XP`;
  $("#sidebarDetective").textContent = state.dashboard.detectiveName;
  $("#dashboardCaseId").textContent = `CASE ${state.dashboard.caseId}`;
  $("#statScore").textContent = state.dashboard.score;
  $("#statEvidence").textContent = state.dashboard.evidenceCollected;
  $("#statLocations").textContent = state.dashboard.locationsInvestigated;
  $("#statInterviews").textContent = state.dashboard.interrogationsCompleted;
  $("#objectiveText").textContent = state.dashboard.objective;
}
async function refreshGame() {
  if (!state.gameId) return;
  const [dashboard, locations, suspects, evidence, caseFile] = await Promise.all([
    api.getDashboard(state.gameId), api.getLocations(state.gameId), api.getSuspects(state.gameId), api.getEvidence(state.gameId), api.getCase(state.gameId)
  ]);
  Object.assign(state, { dashboard, locations, suspects, evidence, caseFile });
  updateTop(); renderDashboard(); renderSuspects(); renderLocations(); renderEvidence(); renderInterrogationSelectors(); renderDeduction();
}
function renderDashboard() {
  const recent = $("#recentEvidence");
  recent.innerHTML = state.evidence.length ? state.evidence.slice(-4).reverse().map(item => `<div class="recent-item"><span class="recent-index">${String(item.id).padStart(2,"0")}</span><div><strong>${esc(item.name)}</strong><small>${esc(item.discoveryLocation)}</small></div><span class="recent-arrow">↗</span></div>`).join("") : `<p class="empty-note">No evidence collected yet. Begin with Studio 7.</p>`;
  $("#dashboardSuspects").innerHTML = state.suspects.map((person, i) => `<button class="mini-suspect" data-open-suspect="${esc(person.name)}"><span class="mini-avatar avatar-${i+1}">${esc(person.name[0])}</span><span><strong>${esc(person.name)}</strong><small>${person.interviewed ? "INTERVIEWED" : "AWAITING INTERVIEW"}</small></span><b>↗</b></button>`).join("");
  $$('[data-open-suspect]').forEach(button => button.addEventListener("click", () => { selectedSuspect = button.dataset.openSuspect; renderSuspects(); showScreen("suspects"); scrollDossier(); }));
}
function renderSuspects() {
  $("#suspectGrid").innerHTML = state.suspects.map((person, i) => `<button class="suspect-card ${selectedSuspect === person.name ? "selected" : ""}" data-suspect="${esc(person.name)}"><span class="suspect-index">PERSON OF INTEREST / 0${i+1}</span><span class="suspect-avatar avatar-${i+1}">${esc(person.name[0])}</span><strong>${esc(person.name)}</strong><small>${esc(person.relationship)}</small><span class="suspicion-meter"><i style="width:${Math.max(0,Math.min(100,person.suspicionLevel))}%"></i></span><span class="suspect-status">${person.interviewed ? "STATEMENT RECORDED" : "DOSSIER AVAILABLE"}</span></button>`).join("");
  $$("[data-suspect]").forEach(button => button.addEventListener("click", () => { selectedSuspect = button.dataset.suspect; renderSuspects(); renderDossier(); }));
  renderDossier();
}
function renderDossier() {
  const person = state.suspects.find(item => item.name === selectedSuspect);
  const panel = $("#suspectDossier");
  if (!person) { panel.innerHTML = `<div class="empty-state"><span>◉</span><h2>Select a person of interest</h2><p>Open a dossier to review known details and begin an interview.</p></div>`; return; }
  panel.innerHTML = `<div class="dossier-content"><div class="dossier-portrait"><span class="suspect-avatar avatar-${state.suspects.indexOf(person)+1}">${esc(person.name[0])}</span><small>FILE / ${String(state.suspects.indexOf(person)+1).padStart(2,"0")}</small></div><div class="dossier-details"><p class="eyebrow"><i></i> PERSON OF INTEREST</p><h2>${esc(person.name)}</h2><p class="muted-copy">${esc(person.relationship)}</p><div class="dossier-facts"><div><small>AGE</small><strong>${esc(person.age)}</strong></div><div><small>GENDER</small><strong>${esc(person.gender)}</strong></div><div><small>STATEMENT STATUS</small><strong>${person.interviewed ? "RECORDED" : "NOT YET RECORDED"}</strong></div><div><small>SUSPICION</small><strong>${esc(person.suspicionLevel)} / 100</strong></div></div>${person.interviewed ? `<div class="revealed-facts"><p><small>MOTIVE ON RECORD</small>${esc(person.motive || "No motive recorded.")}</p><p><small>STATED ALIBI</small>${esc(person.alibi || "No alibi recorded.")}</p></div>` : `<p class="locked-note">Interview this person to reveal their motive and alibi.</p>`}<button id="dossierInterview" class="btn btn-red">Open interrogation <span>↗</span></button></div></div>`;
  $("#dossierInterview").addEventListener("click", () => { showScreen("interrogation"); $("#interviewSuspect").value = selectedSuspect; loadQuestions(selectedSuspect); });
}
function scrollDossier(){renderDossier();$("#suspectDossier").scrollIntoView({behavior:"smooth",block:"start"});}
function renderLocations() {
  const map = $("#locationMap");
  $$(".location-node", map).forEach(el => el.remove());
  state.locations.forEach((location, i) => {
    const button = document.createElement("button"); button.type="button"; button.className=`location-node ${location.investigated?"investigated":""}`; button.dataset.location=location.name;
    button.innerHTML=`<span>0${i+1} / ${location.investigated?"SEARCHED":"UNSEARCHED"}</span><strong>${esc(location.name)}</strong><small>${location.investigated?"Review findings":"Inspect location"} ↗</small>`;
    button.addEventListener("click",()=>selectLocation(location.name)); map.append(button);
  });
  if (selectedLocation && state.locations.some(item=>item.name===selectedLocation)) showLocationDetail(selectedLocation);
}
function showLocationDetail(name, message="") {
  const location=state.locations.find(item=>item.name===name); if(!location)return;
  const found=state.evidence.filter(item=>item.discoveryLocation===name);
  $("#locationDetail").innerHTML=`<p class="eyebrow"><i></i> LOCATION FILE</p><span class="case-stamp-small">${location.investigated?"SEARCH COMPLETE":"NOT YET SEARCHED"}</span><h2>${esc(location.name)}</h2><p class="large-copy">${esc(location.description)}</p>${found.length?`<div class="location-findings"><small>RECOVERED HERE</small>${found.map(e=>`<p><b>${String(e.id).padStart(2,"0")}</b> ${esc(e.name)}</p>`).join("")}</div>`:`<p class="muted-copy">${location.investigated?"No new evidence was recorded here.":"Search this area to reveal evidence connected to the case."}</p>`}${message?`<p class="feedback">${esc(message)}</p>`:""}<button id="investigateLocation" class="btn btn-red" ${location.investigated?"":""}>${location.investigated?"Search again":"Investigate location"} <span>↗</span></button>`;
  $("#investigateLocation").addEventListener("click",async()=>{const button=$("#investigateLocation");button.disabled=true;try{const result=await api.investigate(state.gameId,name);await refreshGame();showLocationDetail(name,result.message);selectedLocation=name;}catch(err){showLocationDetail(name,err.message);}finally{const b=$("#investigateLocation");if(b)b.disabled=false;}});
}
async function selectLocation(name){selectedLocation=name;showLocationDetail(name);}
function renderEvidence(){
  $("#evidenceCount").textContent=`${state.evidence.length} ITEMS`;
  const grid=$("#evidenceGrid");
  grid.innerHTML=state.evidence.length?state.evidence.map(item=>`<button class="evidence-card" data-evidence="${item.id}"><span class="evidence-id">EVIDENCE / ${String(item.id).padStart(2,"0")}</span><span class="evidence-symbol">${item.important?"✦":"⌕"}</span><strong>${esc(item.name)}</strong><p>${esc(item.description)}</p><small>FOUND: ${esc(item.discoveryLocation)}</small></button>`).join(""):`<div class="empty-evidence">No evidence recovered yet. Search a location to begin.</div>`;
  $$("[data-evidence]").forEach(button=>button.addEventListener("click",()=>openEvidence(Number(button.dataset.evidence))));
}
function openEvidence(id){const item=state.evidence.find(e=>e.id===id);if(!item)return;$("#inspectEvidenceId").textContent=`EVIDENCE / ${String(item.id).padStart(2,"0")}`;$("#inspectEvidenceName").textContent=item.name;$("#inspectEvidenceDescription").textContent=item.description;$("#inspectEvidenceLocation").textContent=item.discoveryLocation;$("#evidenceDialog").showModal();}
function renderInterrogationSelectors(){
  const suspectSelect=$("#interviewSuspect"),current=suspectSelect.value;
  suspectSelect.innerHTML=`<option value="">Choose a person…</option>`+state.suspects.map(p=>`<option value="${esc(p.name)}">${esc(p.name)}</option>`).join("");
  if(state.suspects.some(p=>p.name===current))suspectSelect.value=current;else if(selectedSuspect)suspectSelect.value=selectedSuspect;
  const evidenceSelect=$("#evidenceToPresent"),currentEvidence=evidenceSelect.value;
  evidenceSelect.innerHTML=state.evidence.length?`<option value="">Choose evidence…</option>`+state.evidence.map(e=>`<option value="${e.id}">${esc(e.name)}</option>`).join(""):`<option value="">Collect evidence first</option>`;
  if(currentEvidence)evidenceSelect.value=currentEvidence;
}
async function loadQuestions(name){
  selectedSuspect=name;const person=state.suspects.find(p=>p.name===name);$("#interviewName").textContent=person?.name||"Select a suspect";$("#interviewRole").textContent=person?.relationship||"";$("#interviewStatus").textContent=person?.interviewed?"STATEMENT RECORDED":"INTERVIEW OPEN";
  const log=$("#dialogueLog");if(!name){$("#questionOptions").innerHTML="";log.innerHTML=`<p class="empty-note">Select a person of interest to begin the conversation.</p>`;return;}
  try{questionList=await api.getQuestions(state.gameId,name);$("#questionOptions").innerHTML=questionList.map(q=>`<button class="question-button" data-question="${esc(q.id)}">${esc(q.text)}</button>`).join("");$$("[data-question]").forEach(btn=>btn.addEventListener("click",()=>askQuestion(name,btn.dataset.question)));if(!log.querySelector(".dialogue-entry"))log.innerHTML=`<p class="empty-note">Choose a question to begin the interview with ${esc(name)}.</p>`;}catch(err){$("#questionOptions").innerHTML=`<p class="feedback">${esc(err.message)}</p>`;}
}
function appendDialogue(speaker,text){const log=$("#dialogueLog");log.querySelector(".empty-note")?.remove();const entry=document.createElement("div");entry.className=`dialogue-entry ${speaker}`;entry.textContent=text;log.append(entry);log.scrollTop=log.scrollHeight;}
function appendResponse(message){const parts=String(message).split("\n\n");if(parts[0].startsWith("DETECTIVE:")){appendDialogue("detective",parts.shift().replace(/^DETECTIVE:\s*/,""));}else if(parts[0].startsWith("PRESENTED:")){appendDialogue("system",parts.shift());}appendDialogue("suspect",parts.join("\n\n"));}
async function askQuestion(name,id){try{const result=await api.askQuestion(state.gameId,name,id);appendResponse(result.message);await refreshGame();$("#interviewStatus").textContent="STATEMENT RECORDED";}catch(err){appendDialogue("system",err.message);}}
$("#presentEvidenceButton").addEventListener("click",async()=>{const name=$("#interviewSuspect").value,id=Number($("#evidenceToPresent").value);if(!name||!id){appendDialogue("system","Choose a suspect and collected evidence first.");return;}try{const result=await api.presentEvidence(state.gameId,name,id);appendResponse(result.message);await refreshGame();}catch(err){appendDialogue("system",err.message);}});
function renderDeduction(){
  $("#theorySuspects").innerHTML=state.suspects.map(person=>`<button class="theory-suspect ${theorySuspect===person.name?"selected":""}" data-theory-suspect="${esc(person.name)}"><strong>${esc(person.name)}</strong><small>${person.interviewed?"INTERVIEW RECORDED":"NOT INTERVIEWED"}</small></button>`).join("");
  $$("[data-theory-suspect]").forEach(btn=>btn.addEventListener("click",()=>{theorySuspect=btn.dataset.theorySuspect;renderDeduction();}));
  $("#availableEvidence").innerHTML=state.evidence.length?state.evidence.map(item=>`<button draggable="true" class="evidence-pick ${linkedEvidence.has(item.id)?"selected":""}" data-pick-evidence="${item.id}">${esc(item.name)}<small>EVIDENCE / ${String(item.id).padStart(2,"0")}</small></button>`).join(""):`<p class="empty-note">Collect evidence at a location before building a theory.</p>`;
  $$("[data-pick-evidence]").forEach(btn=>{btn.addEventListener("click",()=>toggleEvidence(Number(btn.dataset.pickEvidence)));btn.addEventListener("dragstart",event=>event.dataTransfer.setData("text/plain",btn.dataset.pickEvidence));});
  const zone=$("#theoryEvidence");zone.classList.add("drop-zone");zone.innerHTML=linkedEvidence.size?[...linkedEvidence].map(id=>{const e=state.evidence.find(item=>item.id===id);return e?`<button class="linked-evidence" data-unlink="${id}"><span>×</span>${esc(e.name)}</button>`:"";}).join(""):`<p class="drop-hint">Drop evidence here or select evidence cards below.</p>`;
  $$("[data-unlink]").forEach(btn=>btn.addEventListener("click",()=>toggleEvidence(Number(btn.dataset.unlink))));
  $("#linkedCount").textContent=`${linkedEvidence.size} SELECTED`;
  zone.ondragover=event=>{event.preventDefault();zone.classList.add("dragover")};zone.ondragleave=()=>zone.classList.remove("dragover");zone.ondrop=event=>{event.preventDefault();zone.classList.remove("dragover");const id=Number(event.dataTransfer.getData("text/plain"));if(state.evidence.some(e=>e.id===id)){linkedEvidence.add(id);renderDeduction();}};
}
function toggleEvidence(id){if(linkedEvidence.has(id))linkedEvidence.delete(id);else linkedEvidence.add(id);renderDeduction();}
$("#submitTheory").addEventListener("click",async()=>{const feedback=$("#theoryFeedback");if(!theorySuspect){feedback.textContent="Choose a person of interest first.";return;}try{const result=await api.submitTheory(state.gameId,theorySuspect,[...linkedEvidence]);feedback.textContent=result.message;feedback.classList.toggle("solved",result.solved);await refreshGame();if(result.solved){setTimeout(()=>showScreen("report"),900);}}catch(err){feedback.textContent=err.message;}});
async function loadReport(){if(!state.gameId)return;try{const report=await api.getReport(state.gameId);$("#reportHero").classList.toggle("solved",report.solved);$("#reportStatus").textContent=report.solved?"CASE SOLVED":"CASE IN PROGRESS";$("#reportTitle").textContent=report.solved?`Solved: ${report.culprit}`:report.title;$("#reportExplanation").textContent=report.explanation;$("#reportDetective").textContent=report.detectiveName;$("#reportScore").textContent=report.score;$("#reportEvidenceCount").textContent=report.evidenceCollected;$("#reportInterviewCount").textContent=report.interrogationsCompleted;$("#reportEvidence").innerHTML=report.evidence.length?report.evidence.map(e=>`<div class="recent-item"><span class="recent-index">${String(e.id).padStart(2,"0")}</span><div><strong>${esc(e.name)}</strong><small>${esc(e.discoveryLocation)}</small></div></div>`).join(""):`<p class="empty-note">No evidence recorded.</p>`;}catch(err){$("#reportExplanation").textContent=err.message;}}

$("#beginButton").addEventListener("click",()=>{showError("");$("#registrationDialog").showModal();$("#detectiveName").focus();});
$("#closeDialog").addEventListener("click",()=>$("#registrationDialog").close());
$("#registrationDialog").addEventListener("click",event=>{if(event.target===$("#registrationDialog"))$("#registrationDialog").close();});
$("#registrationForm").addEventListener("submit",async event=>{
  event.preventDefault();showError("");const name=$("#detectiveName").value.trim();const ageText=$("#detectiveAge").value.trim();const age=ageText?Number(ageText):null;const gender=$("#detectiveGender").value.trim()||null;
  if(!name){showError("Enter your detective name to open the case file.");return;}if(age!==null&&(!Number.isInteger(age)||age<1||age>130)){showError("Enter an age from 1 to 130, or leave it blank.");return;}
  const button=$("#registerButton");button.disabled=true;try{const game=await api.createGame({name,age,gender});setGameId(game.gameId);state.caseFile=game.caseFile;await refreshGame();$("#briefCaseId").textContent=`CASE ${game.caseFile.caseId}`;$("#briefTitle").textContent=game.caseFile.title;$("#briefDescription").textContent=game.caseFile.description;$("#briefVictim").textContent=game.caseFile.victimName;$("#briefVictimName").textContent=game.caseFile.victimName;$("#briefVictimOccupation").textContent=game.caseFile.victimOccupation;$("#briefLastSeen").textContent=game.caseFile.lastSeenLocation;$("#coverScreen").hidden=true;$("#gameApp").hidden=false;$("#registrationDialog").close();showScreen("briefing");}catch(err){showError(err.message||"Could not reach the Java game server. Start the project and try again.");}finally{button.disabled=false;}
});
$("#enterDashboard").addEventListener("click",()=>showScreen("dashboard"));
$$("[data-screen]").forEach(button=>button.addEventListener("click",()=>showScreen(button.dataset.screen)));
$("#closeEvidenceDialog").addEventListener("click",()=>$("#evidenceDialog").close());$("#inspectCloseButton").addEventListener("click",()=>$("#evidenceDialog").close());
$("#interviewSuspect").addEventListener("change",event=>{ $("#dialogueLog").innerHTML="";loadQuestions(event.target.value); });

if(state.gameId){
  $("#coverScreen").hidden=true;
  $("#gameApp").hidden=false;
  refreshGame().then(()=>{showScreen("dashboard");}).catch(()=>{setGameId(null);$("#gameApp").hidden=true;});
}

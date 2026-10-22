const API = "/flights";
const form = document.getElementById("flight-form");
const status = document.getElementById("status");
const message = document.getElementById("message");
let allFlights = [];
let editingId = null;

async function request(path = "", options = {}) {
    const response = await fetch(`${API}${path}`, options);
    if (!response.ok) {
        const problem = await response.json().catch(() => ({}));
        const errors = Object.values(problem.errors || {}).join("; ");
        throw new Error(errors || problem.detail || `Request failed (${response.status})`);
    }
    return response.status === 204 ? null : response.json();
}

async function loadFlights() {
    try {
        allFlights = await request();
        status.textContent = "API: online";
        renderFlights();
    } catch (error) {
        status.textContent = "API: unavailable";
        message.textContent = error.message;
        throw error;
    }
}

function renderFlights() {
    const search = document.getElementById("search").value.toLowerCase();
    const tbody = document.getElementById("flights");
    tbody.replaceChildren();
    const flights = allFlights.filter(f => f.flightNumber.toLowerCase().includes(search));
    for (const flight of flights) {
        const row = document.createElement("tr");
        for (const [label, value] of [["ID", flight.id], ["Number", flight.flightNumber],
            ["From", flight.fromAirport], ["To", flight.toAirport],
            ["Departure time", flight.departureTime.replace("T", " ")]]) {
            const cell = document.createElement("td");
            cell.dataset.label = label;
            cell.textContent = value;
            row.append(cell);
        }
        const actions = document.createElement("td");
        actions.dataset.label = "Actions";
        const edit = document.createElement("button");
        edit.textContent = "Edit";
        edit.addEventListener("click", () => editFlight(flight));
        const remove = document.createElement("button");
        remove.textContent = "Delete";
        remove.addEventListener("click", () => deleteFlight(flight.id));
        const controls = document.createElement("div");
        controls.append(edit, remove);
        actions.append(controls);
        row.append(actions);
        tbody.append(row);
    }
    if (!flights.length) {
        const cell = document.createElement("td");
        cell.colSpan = 6;
        cell.textContent = "No flights found.";
        const row = document.createElement("tr");
        row.append(cell);
        tbody.append(row);
    }
}

function resetForm() {
    editingId = null;
    form.reset();
    document.getElementById("form-title").textContent = "Add flight";
    document.getElementById("save").textContent = "Add";
    document.getElementById("cancel").hidden = true;
}

function editFlight(flight) {
    editingId = flight.id;
    document.getElementById("number").value = flight.flightNumber;
    document.getElementById("origin").value = flight.fromAirport;
    document.getElementById("destination").value = flight.toAirport;
    document.getElementById("time").value = flight.departureTime;
    document.getElementById("form-title").textContent = `Edit flight ${flight.id}`;
    document.getElementById("save").textContent = "Save";
    document.getElementById("cancel").hidden = false;
    document.getElementById("number").focus();
}

form.addEventListener("submit", async event => {
    event.preventDefault();
    const save = document.getElementById("save");
    save.disabled = true;
    message.textContent = "";
    const id = editingId;
    const flight = {
        flightNumber: document.getElementById("number").value.trim(),
        fromAirport: document.getElementById("origin").value.trim(),
        toAirport: document.getElementById("destination").value.trim(),
        departureTime: document.getElementById("time").value
    };
    try {
        await request(id === null ? "" : `/${id}`, {
            method: id === null ? "POST" : "PUT",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(flight)
        });
        resetForm();
        await loadFlights();
        message.textContent = id === null ? "Flight added." : "Flight updated.";
    } catch (error) {
        message.textContent = error.message;
    } finally {
        save.disabled = false;
    }
});

async function deleteFlight(id) {
    try {
        await request(`/${id}`, {method: "DELETE"});
        if (editingId === id) resetForm();
        await loadFlights();
        message.textContent = "Flight deleted.";
    } catch (error) {
        message.textContent = error.message;
    }
}

document.getElementById("cancel").addEventListener("click", resetForm);
document.getElementById("search").addEventListener("input", renderFlights);
document.getElementById("refresh").addEventListener("click", () => loadFlights().catch(() => {}));
loadFlights().catch(() => {});

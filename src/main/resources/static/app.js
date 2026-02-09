const API = "http://localhost:8080/flights";
let allFlights = [];

function loadFlights() {
    fetch(API)
        .then(r => r.json())
        .then(data => {
            allFlights = data;
            renderFlights();
        });
}

function renderFlights() {
    const search = document.getElementById("search").value.toLowerCase();
    const tbody = document.getElementById("flights");
    tbody.innerHTML = "";

    allFlights
        .filter(f => f.flightNumber.toLowerCase().includes(search))
        .forEach(f => {
            tbody.innerHTML += `
            <tr>
                <td>${f.flightId}</td>
                <td>${f.flightNumber}</td>
                <td>${f.origin}</td>
                <td>${f.destination}</td>
                <td>${f.departureTime}</td>
                <td>${f.capacity}</td>
                <td><button onclick="deleteFlight(${f.flightId})">❌</button></td>
            </tr>`;
        });
}

function addFlight() {
    const flight = {
        flightNumber: number.value,
        origin: origin.value,
        destination: destination.value,
        departureTime: time.value,
        capacity: capacity.value
    };

    fetch(API, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(flight)
    }).then(loadFlights);
}

function deleteFlight(id) {
    fetch(`${API}/${id}`, {method: "DELETE"})
        .then(loadFlights);
}

loadFlights();

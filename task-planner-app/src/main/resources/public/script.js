console.log("Script loaded successfully!");

async function fetchDemoData() {
    const responseDiv = document.getElementById('server-response');
    responseDiv.textContent = "Loading data...";

    try {
        const response = await fetch('/data.json');
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();

        responseDiv.textContent = JSON.stringify(data, null, 2);
        responseDiv.style.borderLeft = "5px solid #2ecc71";

    } catch (error) {
        console.error("Fetch error:", error);
        responseDiv.textContent = "Error loading data: " + error.message;
        responseDiv.style.borderLeft = "5px solid #e74c3c";
    }
}

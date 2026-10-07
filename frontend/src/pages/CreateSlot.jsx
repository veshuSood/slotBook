import { useState } from "react";
import { apiFetch } from "../api/api";

function CreateSlot({ onSlotCreated }) {
  const [date, setDate] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();

    const response = await apiFetch(
      "http://localhost:8080/api/slots",
      {
        method: "POST",
        body: JSON.stringify({
          date: date,
          startTime: startTime,
          endTime: endTime,
        }),
      }
    );

    if (!response.ok) {
      alert("Failed to create slot");
      return;
    }

    const data = await response.json();

    console.log("Created slot:", data);

    alert("Slot created successfully!");

    await  onSlotCreated();

    setDate("");
    setStartTime("");
    setEndTime("");
  }

  return (
    <div>
      <h2>Create Slot</h2>

      <form onSubmit={handleSubmit}>
        <div>
          <label>Date</label>
          <input
            type="date"
            value={date}
            onChange={(event) => setDate(event.target.value)}
          />
        </div>

        <div>
          <label>Start Time</label>
          <input
            type="time"
            value={startTime}
            onChange={(event) => setStartTime(event.target.value)}
          />
        </div>

        <div>
          <label>End Time</label>
          <input
            type="time"
            value={endTime}
            onChange={(event) => setEndTime(event.target.value)}
          />
        </div>

        <button type="submit">
          Create Slot
        </button>
      </form>
    </div>
  );
}

export default CreateSlot;
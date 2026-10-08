import { useState,useEffect } from "react";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import { apiFetch } from "./api/api";
import SlotCard from "./components/SlotCard";
import "./App.css";
import CreateSlot from "./pages/CreateSlot";
import { getUserRole } from "./api/auth";
import Register from "./pages/Register"; 
import OAuthSuccess from "./pages/OAuthSuccess";
function App() {
  const [page, setPage] = useState("home");
  const [slots, setSlots] = useState([]);
  const [myBooking, setMyBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(
  !!localStorage.getItem("token")
  );
  const [role, setRole] = useState(
  getUserRole()
  );
  const [bookingSlotId, setBookingSlotId] = useState(null);

  function formatDate(dateString) {
  const date = new Date(dateString + "T00:00:00");

  return date.toLocaleDateString("en-IN", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

  async function fetchSlots() {
  setLoading(true);
  setError(null);

  try {
    const response = await apiFetch("/api/slots");

    if (!response.ok) {
      throw new Error("Failed to load slots");
    }

    const data = await response.json();

    setSlots(data);
  } catch (error) {
    console.error(error);
    setError("Failed to load slots. Please try again.");
  } finally {
    setLoading(false);
  }
}
  useEffect(() => {
  fetchSlots();
}, []);

async function bookSlot(slotId) {
  if (!localStorage.getItem("token")) {
  setPage("login");
  return;
}
  setBookingLoading(true);
  setBookingSlotId(slotId);

  try {
    const idempotencyKey = crypto.randomUUID();

    const response = await apiFetch(
      `/api/slots/${slotId}/book`,
      {
        method: "POST",
        headers: {
          "Idempotency-Key": idempotencyKey,
        },
      }
    );

    if (!response.ok) {
  const message = await response.text();

  if (response.status === 409) {
    alert("Sorry, this slot is no longer available.");
  } else if (response.status === 401) {
    alert("Your session has expired. Please login again.");
    handleLogout();
  } else {
    alert(`Booking failed: ${response.status}\n${message}`);
  }

  return;
}

    const data = await response.json();

    console.log("Booking:", data);

    setMyBooking(data);
    await fetchSlots();

  } catch (error) {
    console.error(error);
    alert("Failed to book slot. Please try again.");
  } finally {
    setBookingLoading(false);
    setBookingSlotId(null);
  }
}
function handleLogout() {
  localStorage.removeItem("token");
  setIsLoggedIn(false);
  setRole(null)
  setMyBooking(null);
  setPage("home");
}
async function confirmBooking(slotId) {
  const response = await apiFetch(
    `/api/slots/${slotId}/confirm`,
    {
      method: "POST",
    }
  );

  const text = await response.text();

  if (!response.ok) {
    if (response.status === 409) {
      alert("Your hold has expired or is no longer available.");
      setMyBooking(null);
      await fetchSlots();
    } else if (response.status === 401) {
      alert("Your session has expired. Please login again.");
      handleLogout();
    } else {
      alert(`Confirm failed: ${response.status}\n${text}`);
    }

    return;
  }

  const data = JSON.parse(text);

  console.log("Booking:", data);

  setMyBooking(data);

  await fetchSlots();
}
async function deleteSlot(slotId) {
  const confirmed = window.confirm(
    "Are you sure you want to delete this slot?"
  );

  if (!confirmed) {
    return;
  }

  try {
    const response = await apiFetch(
      `/api/slots/${slotId}`,
      {
        method: "DELETE",
      }
    );

    if (!response.ok) {
      const message = await response.text();

      if (response.status === 409) {
        alert("This slot cannot be deleted because it is booked or held.");
      } else if (response.status === 401) {
        alert("Your session has expired. Please login again.");
        handleLogout();
      } else if (response.status === 403) {
        alert("Only admins can delete slots.");
      } else {
        alert(`Delete failed: ${response.status}\n${message}`);
      }

      return;
    }

    alert("Slot deleted successfully.");

    await fetchSlots();

  } catch (error) {
    console.error(error);
    alert("Something went wrong while deleting the slot.");
  }
}
  return (

    <div>
      <Navbar
  isLoggedIn={isLoggedIn}
  onLogin={() => setPage("login")}
  onRegister={()=> setPage("register")}
  onLogout={handleLogout}

/>
{window.location.pathname === "/oauth-success" && (
  <OAuthSuccess
    onLoginSuccess={() => {
      setIsLoggedIn(true);
      setRole(getUserRole());
      setPage("home");
      fetchSlots();

      window.history.replaceState({}, "", "/");
    }}
  />
)}

      {page === "home" && (
        <>
          <h1>Book an Appointment</h1>
<p>Select a time slot to book an appointment.</p>
        {isLoggedIn && role === "ROLE_ADMIN" && (
  <button onClick={() => setPage("create-slot")}>
    Create Slot
  </button>
)}


          <div className="slots-section">
  <h2>Available Slots</h2>

  {loading ? (
  <p>Loading slots...</p>
) : error ? (
  <p>{error}</p>
) : (
  <div className="slot-days">
  {Object.entries(
    slots.reduce((groups, slot) => {
      if (!groups[slot.date]) {
        groups[slot.date] = [];
      }

      groups[slot.date].push(slot);

      return groups;
    }, {})
  ).map(([date, dateSlots]) => (
    <section key={date} className="slot-day">
      <h2>{formatDate(date)}</h2>

      <div className="slot-grid">
        {dateSlots.map((slot) => (
          <SlotCard
            key={slot.id}
            slot={slot}
            booking={myBooking}
            onBook={bookSlot}
            onConfirm={confirmBooking}
            onDelete={deleteSlot}
            isAdmin={role === "ROLE_ADMIN"}
            bookingLoading={bookingLoading}
            bookingSlotId={bookingSlotId}
          />
        ))}
      </div>
    </section>
  ))}
</div>
)}
</div>
</>
      )}

      {page === "login" && (
  <Login
    onLoginSuccess={() => {
      setIsLoggedIn(true);
      setRole(getUserRole());
      setPage("home");
      fetchSlots();
    }}
  />
)}
      {page === "register" && (
  <Register
    onRegisterSuccess={() => setPage("home")}
  />
)}
      {page === "create-slot" && (
  <CreateSlot
    onSlotCreated={async () => {
      await fetchSlots();
      setPage("home");
    }}
  />
)}
    </div>
  );
}

export default App;
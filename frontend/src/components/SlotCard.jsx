function SlotCard({
  slot,
  booking,
  onBook,
  onConfirm,
  onDelete,
  isAdmin,
  bookingLoading,
  bookingSlotId,
}) {

  function formatTime(timeString) {
    const [hours, minutes] = timeString.split(":");

    const date = new Date();
    date.setHours(Number(hours), Number(minutes), 0, 0);

    return date.toLocaleTimeString("en-IN", {
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
    });
  }

  return (
    <div className="slot-card">

      <h3>
        {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
      </h3>

      <p>
        Slot Status:{" "}
        <strong className={`status ${slot.status.toLowerCase()}`}>
          {slot.status}
        </strong>
      </p>

      {booking && (
        <p>
          Booking Status:{" "}
          <strong
            className={`status booking-${booking.status.toLowerCase()}`}
          >
            {booking.status}
          </strong>
        </p>
      )}

      {slot.status === "AVAILABLE" && (
        <button
          onClick={() => onBook(slot.id)}
          disabled={bookingLoading && bookingSlotId === slot.id}
        >
          {bookingLoading && bookingSlotId === slot.id
            ? "Booking..."
            : "Book Slot"}
        </button>
      )}

      {booking?.status === "HELD" && (
        <button onClick={() => onConfirm(slot.id)}>
          Confirm Booking
        </button>
      )}

      {isAdmin && slot.status === "AVAILABLE" && (
        <button onClick={() => onDelete(slot.id)}>
          Delete Slot
        </button>
      )}

      {booking?.status === "CONFIRMED" && (
        <p>Confirmed ✅</p>
      )}

    </div>
  );
}

export default SlotCard;
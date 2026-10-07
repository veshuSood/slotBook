function SlotCard({ slot,
  booking,
  onBook,
  onConfirm,
  onDelete,
  isAdmin,
  bookingLoading,
  bookingSlotId }) {
  return (
    <div className="slot-card">
      <h3>{slot.date}</h3>

      <p>
        {slot.startTime} - {slot.endTime}
      </p>

      <p>
  Slot Status:{" "}
  <strong className={`status ${slot.status.toLowerCase()}`}>
    {slot.status}
  </strong>
</p>

{booking && (
  <p>
    Booking Status:{" "}
    <strong className={`status booking-${booking.status.toLowerCase()}`}>
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
function Navbar({ isLoggedIn, onLogin,onRegister, onLogout }) {
  return (
    <nav>
      <h2>SlotBook</h2>

      <div>
        {isLoggedIn ? (
          <button onClick={onLogout}>
            Logout
          </button>
        ) : (
          <>
            <button onClick={onLogin}>
              Login
            </button>

            <button onClick={onRegister}>
              Register
            </button>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
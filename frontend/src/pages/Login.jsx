import { useState } from "react";
import { apiFetch } from "../api/api";
function Login({ onLoginSuccess }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    console.log("LOGIN BUTTON WORKED");

    const response = await apiFetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email: email,
        password: password,
      }),
    });

    const data = await response.json();

localStorage.setItem("token", data.token);
onLoginSuccess();

console.log("JWT stored:", data.token);
  }

  return (
    <div>
      <h1>Login</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label>Email</label>
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>

        <div>
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </div>

        <button type="submit">Login</button>
        <button
  type="button"
  onClick={() => {
    window.location.href =
  "https://slotbook-backend-udag.onrender.com/oauth2/authorization/google";
  }}
>
  Continue with Google
</button>
      </form>
    </div>
  );
}

export default Login;
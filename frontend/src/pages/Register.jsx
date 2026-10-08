
import { useState } from "react";
import { apiFetch } from "../api/api";

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();

   const response = await apiFetch("/api/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name: name,
      email: email,
      password: password,
    }),
  }
);

   if (!response.ok) {
  const text = await response.text();

  console.log("Register status:", response.status);
  console.log("Register response:", text);

  alert(`Registration failed: ${response.status}\n${text}`);
  return;
}

    alert("Registration successful! Please login.");

    setName("");
    setEmail("");
    setPassword("");
  }

  return (
    <div>
      <h1>Register</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label>Name</label>
          <input
            type="text"
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
        </div>

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

        <button type="submit">
          Register
        </button>
      </form>
    </div>
  );
}

export default Register;
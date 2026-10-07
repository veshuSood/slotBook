import { useEffect } from "react";

function OAuthSuccess({ onLoginSuccess }) {

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);

    const token = params.get("token");

    if (!token) {
      console.error("No JWT received from Google login");
      return;
    }

    localStorage.setItem("token", token);

    onLoginSuccess();
  }, [onLoginSuccess]);

  return (
    <div>
      <h2>Signing you in...</h2>
    </div>
    
  );
}

export default OAuthSuccess;
import { Link } from "react-router-dom";
import { Button } from "../components/Button.jsx";
import { useAuth } from "../auth/AuthContext.jsx";

export function LandingPage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="landing">
      <header className="landing-nav">
        <span className="brand">CollabDocs</span>
        <div className="landing-nav-actions">
          {isAuthenticated ? (
            <Link to="/documents">
              <Button>Open dashboard</Button>
            </Link>
          ) : (
            <>
              <Link to="/login">
                <Button variant="secondary">Sign in</Button>
              </Link>
              <Link to="/register">
                <Button>Get started</Button>
              </Link>
            </>
          )}
        </div>
      </header>

      <section className="landing-hero">
        <div className="landing-copy">
          <p className="eyebrow">Real-time collaborative editing</p>
          <h1>Write together, stay in sync</h1>
          <p className="lead">
            Create documents, invite teammates, and edit with operational
            transformation over WebSockets — powered by your Spring Boot
            backend.
          </p>
          <div className="landing-cta">
            <Link to={isAuthenticated ? "/documents" : "/register"}>
              <Button>{isAuthenticated ? "Your documents" : "Create free account"}</Button>
            </Link>
            <Link to="/login">
              <Button variant="secondary">Sign in</Button>
            </Link>
          </div>
        </div>
        <div className="landing-preview" aria-hidden="true">
          <div className="preview-toolbar" />
          <div className="preview-lines">
            <span />
            <span />
            <span className="short" />
          </div>
        </div>
      </section>
    </div>
  );
}

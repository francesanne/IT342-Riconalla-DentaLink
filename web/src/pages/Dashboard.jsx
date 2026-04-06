import Navbar from "../components/Navbar";

function Dashboard() {
  return (
    <>
      <Navbar />
      <div className="container">
        <h2>Welcome to your Dashboard</h2>
        <p>Book and manage your appointments easily.</p>
      </div>
    </>
  );
}

export default Dashboard;
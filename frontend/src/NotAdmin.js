const NotAdmin = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h2 style={{ marginBottom: '10px' }}>Admin Dashboard</h2>

      <p style={{ marginBottom: '10px' }}>
        You are not a verified admin. This dashboard can only be accessed by verified admins.
      </p>

    </div>
  );
};

export default NotAdmin;
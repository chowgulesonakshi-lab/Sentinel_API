import axios from "axios";

const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL ||
    (import.meta.env.PROD
        ? "https://sentinel-api-aawq.onrender.com"
        : "http://localhost:8080");

const api = axios.create({
    baseURL: apiBaseUrl.replace(/\/+$/, ""),
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;

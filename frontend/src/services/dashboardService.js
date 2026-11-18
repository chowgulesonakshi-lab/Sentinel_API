import api from "../api/axios";

export const getDashboard = async () => {
    const response = await api.get("/dashboard");
    return response.data;
};

export const getApiHealth = async () => {
    const response = await api.get("/dashboard/health");
    return response.data;
};

export const getRecentMetrics = async () => {
    const response = await api.get("/dashboard/recent-metrics");
    return response.data;
};

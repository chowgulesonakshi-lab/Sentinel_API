import api from "../api/axios";

export const getAllAlerts = async () => {
    const response = await api.get("/alerts");
    return response.data;
};

export const getActiveAlerts = async () => {
    const response = await api.get("/alerts/active");
    return response.data;
};

export const getRecentAlerts = async () => {
    const response = await api.get("/alerts/recent");
    return response.data;
};

export const getActiveAlertCount = async () => {
    const response = await api.get("/alerts/active-count");
    return response.data;
};

export const getCriticalAlertCount = async () => {
    const response = await api.get("/alerts/critical-count");
    return response.data;
};

export const resolveAlert = async (id) => {
    const response = await api.put(`/alerts/${id}/resolve`);
    return response.data;
};

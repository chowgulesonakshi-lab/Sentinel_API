import api from "../api/axios";

export const getAllMetrics = async () => {
    const response = await api.get("/metrics");
    return response.data;
};

export const getLatestMetrics = async () => {
    const response = await api.get("/metrics/latest");
    return response.data;
};

export const getAverageResponseTime = async () => {
    const response = await api.get("/metrics/average-response-time");
    return response.data;
};

export const getUpCount = async () => {
    const response = await api.get("/metrics/up-count");
    return response.data;
};

export const getDownCount = async () => {
    const response = await api.get("/metrics/down-count");
    return response.data;
};

export const getFailedChecksCount = async () => {
    const response = await api.get("/metrics/failed-count");
    return response.data;
};

export const getSlowApisCount = async () => {
    const response = await api.get("/metrics/slow-count");
    return response.data;
};

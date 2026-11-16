import axios from "axios";
const API_URL = "http://localhost:8080";

const getAuthHeader = () => {
    const token = localStorage.getItem("token");
    return {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };
};

export const getProjects = async () => {
    const response = await axios.get(
        `${API_URL}/projects`,
        getAuthHeader()
    );
    return response.data;
};

export const createProject = async (project) => {
    const response = await axios.post(
        `${API_URL}/projects`,
        project,
        getAuthHeader()
    );
    return response.data;
};

export const updateProject = async (id, project) => {
    const response = await axios.put(
        `${API_URL}/projects/${id}`,
        project,
        getAuthHeader()
    );

    return response.data;
};

export const deleteProject = async (id) => {
    await axios.delete(
        `${API_URL}/projects/${id}`,
        getAuthHeader()
    );
};
import MainLayout from "../layouts/MainLayout";
import { useEffect, useState } from "react";
import ProjectModal from "../components/ProjectComponents/ProjectModal";
import { getProjects, createProject, updateProject, deleteProject } from "../services/projectService";
import { useNavigate } from "react-router-dom";

function Projects() {
    const navigate = useNavigate();
    const [projects, setProjects] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [selectedProject, setSelectedProject] = useState(null);
    useEffect(() => {
        loadProjects();
    }, []);
    const loadProjects = async () => {
        try {
            const data = await getProjects();
            setProjects(data);
        } catch (error) {
            console.error(error);
        }
    };

    const handleSaveProject = async (project) => {
        try {
            if (selectedProject) {
                await updateProject(selectedProject.id, project);
            } else {
                await createProject(project);
            }
            setShowModal(false);
            setSelectedProject(null);
            loadProjects();
        } catch (error) {
            console.error(error);
        }
    };

    const handleDeleteProject = async (id) => {
        const confirmDelete = window.confirm("Are you sure you want to delete this project? All associated services, endpoints, metrics, and alerts will also be deleted.");
        if (!confirmDelete) return;
        try {
            await deleteProject(id);
            console.log("Project deleted successfully");
            loadProjects();
        } catch (error) {
            console.log("Status:", error.response?.status);
            console.log("Response:", error.response?.data);
            console.error(error);
            alert("Failed to delete project: " + (error.response?.data?.message || error.message));
        }
    };

    return (
        <MainLayout>
            <div className="space-y-8">
                {/* Header */}
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
                            Projects
                        </h1>
                        <p className="text-slate-500 dark:text-slate-400 mt-2">
                            Manage all monitoring projects.
                        </p>
                    </div>
                    <button
                        onClick={() => {
                            setSelectedProject(null);
                            setShowModal(true);
                        }}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg"
                    >
                        + New Project
                    </button>
                </div>
                {/* Project Cards */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {projects.map((project) => (
                        <div
                            key={project.id}
                            className="
                            bg-white
                            dark:bg-slate-800
                            rounded-xl
                            shadow-sm
                            border
                            border-slate-200
                            dark:border-slate-700
                            p-6
                            transition-colors
                            duration-300
                            "
                        >
                            <div className="flex justify-between items-start">
                                <div>
                                    <h2 className="text-xl font-semibold text-slate-900 dark:text-white">
                                        {project.name}
                                    </h2>
                                    <p className="text-slate-500 dark:text-slate-400 mt-1">
                                        {project.description}
                                    </p>
                                </div>
                                <span className="bg-green-100 text-green-700 text-sm px-3 py-1 rounded-full">
                                    {project.status}
                                </span>
                            </div>
                            <div className="mt-6 grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-slate-500 dark:text-slate-400 text-sm">
                                        Services
                                    </p>
                                    <h3 className="text-2xl font-bold text-slate-900 dark:text-white">
                                        {project.services}
                                    </h3>
                                </div>
                                <div>
                                    <p className="text-slate-500 dark:text-slate-400 text-sm">
                                        Endpoints
                                    </p>
                                    <h3 className="text-2xl font-bold text-slate-900 dark:text-white">
                                        {project.endpoints}
                                    </h3>
                                </div>
                            </div>
                            <div className="flex justify-end gap-3 mt-6">

                                <button
                                    onClick={() => navigate(`/services?projectId=${project.id}`)}
                                    className="px-4 py-2 border border-blue-500 text-blue-500 rounded-lg
                                    hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-colors"
                                >
                                    View Services
                                </button>


                                <button
                                    onClick={() => {
                                        setSelectedProject(project);
                                        setShowModal(true);
                                    }}
                                    className="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded-lg
                                    hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-900 dark:text-white
                                    transition-colors"
                                >
                                    Edit
                                </button>


                                <button
                                    onClick={() => handleDeleteProject(project.id)}
                                    className="px-4 py-2 border border-red-500 text-red-500 rounded-lg
                                    hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                                >
                                    Delete
                                </button>

                            </div>
                        </div>
                    ))}
                </div>
            </div>
            <ProjectModal
                 isOpen={showModal}
                 project={selectedProject}
                 onClose={() => setShowModal(false)}
                 onSave={handleSaveProject}
             />
        </MainLayout>
    );
}

export default Projects;
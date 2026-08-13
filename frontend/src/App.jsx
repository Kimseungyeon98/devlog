import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/layout/Layout'
import MainPage from './pages/MainPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import PostDetailPage from './pages/PostDetailPage'
import PostCreatePage from './pages/PostCreatePage'
import PostEditPage from './pages/PostEditPage'
import ProfilePage from './pages/ProfilePage'
import NotificationPage from './pages/NotificationPage'
import SeriesPage from './pages/SeriesPage'
import DashboardPage from './pages/DashboardPage'
import SnippetsPage from './pages/SnippetsPage'
import SnippetDetailPage from './pages/SnippetDetailPage'
import SnippetCreatePage from './pages/SnippetCreatePage'
import SnippetEditPage from './pages/SnippetEditPage'
import useAuthStore from './store/authStore'

function PrivateRoute({ children }) {
    const { isLoggedIn } = useAuthStore();
    return isLoggedIn ? children : <Navigate to="/login" />;
}

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Layout />}>
                    <Route index element={<MainPage />} />
                    <Route path="posts/:postId" element={<PostDetailPage />} />
                    <Route path="users/:username" element={<ProfilePage />} />
                    <Route path="series/:seriesId" element={<SeriesPage />} />
                    <Route path="write" element={
                        <PrivateRoute><PostCreatePage /></PrivateRoute>
                    } />
                    <Route path="posts/:postId/edit" element={
                        <PrivateRoute><PostEditPage /></PrivateRoute>
                    } />
                    <Route path="notifications" element={
                        <PrivateRoute><NotificationPage /></PrivateRoute>
                    } />
                    <Route path="dashboard" element={
                        <PrivateRoute><DashboardPage /></PrivateRoute>
                    } />
                    <Route path="snippets" element={<SnippetsPage />} />
                    <Route path="snippets/new" element={
                        <PrivateRoute><SnippetCreatePage /></PrivateRoute>
                    } />
                    <Route path="snippets/:snippetId" element={<SnippetDetailPage />} />
                    <Route path="snippets/:snippetId/edit" element={
                        <PrivateRoute><SnippetEditPage /></PrivateRoute>
                    } />
                </Route>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
            </Routes>
        </BrowserRouter>
    )
}

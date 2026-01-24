package com.example.mxh_application.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mxh_application.presentation.screens.posts.CreatePostScreen
import com.example.mxh_application.presentation.screens.posts.PostDetailScreen
import com.example.mxh_application.presentation.screens.posts.PostListScreen
import com.example.mxh_application.presentation.screens.users.UserDetailScreen
import com.example.mxh_application.presentation.screens.users.UserListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.UserList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.UserList.route) {
            UserListScreen(
                onUserClick = { userId ->
                    navController.navigate(Screen.UserDetail.createRoute(userId))
                },
                onNavigateToPostList = {
                    navController.navigate(Screen.PostList.route)
                },
                onCreatePostClick = {
                    navController.navigate(Screen.CreatePost.route)
                }
            )
        }
        
        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
            UserDetailScreen(
                userId = userId,
                onBackClick = {
                    navController.popBackStack()
                },
                onPostClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                }
            )
        }
        
        composable(route = Screen.PostList.route) {
            PostListScreen(
                onPostClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onCreatePostClick = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: return@composable
            PostDetailScreen(
                postId = postId,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = { userId ->
                    navController.navigate(Screen.UserDetail.createRoute(userId))
                }
            )
        }
        
        composable(route = Screen.CreatePost.route) {
            CreatePostScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPostCreated = {
                    navController.popBackStack()
                }
            )
        }
    }
}

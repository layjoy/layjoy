package com.voicememory.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voicememory.ui.screens.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String = Screen.Record.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 录音主页
        composable(
            route = Screen.Record.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            RecordScreen(navController = navController)
        }
        
        // 时间轴
        composable(
            route = Screen.Timeline.route,
            enterTransition = { 
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = { 
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            TimelineScreen(navController = navController)
        }
        
        // 日历热力图
        composable(
            route = Screen.Calendar.route,
            enterTransition = { 
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = { 
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            CalendarScreen(navController = navController)
        }
        
        // 播放器
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType }
            ),
            enterTransition = { 
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = { 
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            PlayerScreen(
                navController = navController,
                entryId = entryId
            )
        }
        
        // 时光胶囊
        composable(
            route = Screen.Capsule.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            CapsuleScreen(navController = navController)
        }
        
        // 设置
        composable(
            route = Screen.Settings.route,
            enterTransition = { 
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = { 
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            SettingsScreen(navController = navController)
        }
        
        // AI 对话
        composable(
            route = Screen.AIChat.route,
            enterTransition = { 
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = { 
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            AIChatScreen(navController = navController)
        }
        
        // 播放器
        composable(
            route = Screen.Player.route,
            enterTransition = { 
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = { 
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")?.toLongOrNull() ?: 0L
            PlayerScreen(navController = navController, entryId = entryId)
        }
        
        // 趋势分析
        composable(
            route = Screen.Trend.route,
            enterTransition = { 
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = { 
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            TrendScreen(navController = navController)
        }
    }
}

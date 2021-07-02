package uz.muhammadyusuf.kurbonov.myclinic

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.MainActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.PermissionItem
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.theme.AppTheme

class PermissionItemTests {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @ExperimentalPermissionsApi
    @Test
    fun testPermissionDescriptionDisplay() {
        composeTestRule.setContent {
            AppTheme {
                PermissionItem(
                    description = "Test description",
                    onRowClick = { },
                    hasPermission = true,
                    shouldShowRationale = false
                )
            }
        }
        composeTestRule.onNodeWithText("Test description").assertIsDisplayed()
    }

    @ExperimentalPermissionsApi
    @Test
    fun testShowRationale() {
        composeTestRule.setContent {
            AppTheme {
                PermissionItem(
                    description = "Test description",
                    rationale = "Test rationale",
                    onRowClick = { },
                    hasPermission = true,
                    shouldShowRationale = true
                )
            }
        }
        composeTestRule.onNodeWithText("Test rationale").assertIsDisplayed()
    }

    @ExperimentalPermissionsApi
    @Test
    fun testNoShowRationale() {
        composeTestRule.setContent {
            AppTheme {
                PermissionItem(
                    description = "Test description",
                    rationale = "Test rationale",
                    onRowClick = { },
                    hasPermission = true,
                    shouldShowRationale = false
                )
            }
        }
        composeTestRule.onNodeWithText("Test rationale").assertDoesNotExist()
    }

    @ExperimentalPermissionsApi
    @Test
    fun testRowClickable() {
        var clicked = false
        composeTestRule.setContent {
            AppTheme {
                PermissionItem(
                    description = "Test description",
                    rationale = "Test rationale",
                    onRowClick = { clicked = true },
                    hasPermission = true,
                    shouldShowRationale = false
                )
            }
        }
        composeTestRule.onNodeWithText("Test description").onParent().performClick()
        Assert.assertEquals(true, clicked)
    }
}
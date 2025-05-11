package com.ptit.core.category

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CategoryItem(
    val id: String,
    val name: String,
    val displayName: String,
    val imageUrl: String
)

data class CategoryUiState(
    val categories: List<CategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val staticCategories = listOf(
            CategoryItem("67f1ebef40ab575580040f42", "Thời trang", "Thời trang", "https://cdn-icons-png.flaticon.com/512/3205/3205438.png"),
            CategoryItem("67fe76b4a2e13b000d81e942", "Đồ gia dụng", "Đồ gia dụng", "https://cdn-icons-png.flaticon.com/512/7540/7540904.png"),
            CategoryItem("67f1ebe740ab575580040f41", "Nội thất", "Nội thất", "https://cdn-icons-png.flaticon.com/512/1434/1434247.png"),
            CategoryItem("67fe764aa2e13b000d81e941", "Mỹ phẩm", "Mỹ phẩm", "https://cdn-icons-png.flaticon.com/512/3501/3501241.png"),
            CategoryItem("67f1ebde40ab575580040f40", "Điện tử", "Điện tử", "https://cdn-icons-png.flaticon.com/512/3696/3696504.png"),
            CategoryItem("67fe7b38a2e13b000d81e947", "Thực phẩm", "Thực phẩm", "https://cdn-icons-png.flaticon.com/512/7910/7910878.png"),
        )
        _uiState.update {
            it.copy(isLoading = false, categories = staticCategories)
        }
    }
}
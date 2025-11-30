package com.ptit.core.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CategoryUiState {
    object Initial : CategoryUiState()
    object Loading : CategoryUiState()
    data class Success(val categories: List<CategoryDomainEntity>) : CategoryUiState()
    data class Error(val message: String) : CategoryUiState()
}

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Initial)
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val _categoryDetail = MutableStateFlow<CategoryDomainEntity?>(null)
    val categoryDetail: StateFlow<CategoryDomainEntity?> = _categoryDetail.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories(parentCategoryId: String? = null) {
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            when (val result = productRepository.getAllCategories(parentCategoryId)) {
                is Resource.Success -> {
                    _uiState.value = CategoryUiState.Success(result.data.data)
                }
                is Resource.Error -> {
                    _uiState.value = CategoryUiState.Error(result.error.message ?: "Đã xảy ra lỗi")
                }
                is Resource.Idle -> {
                    // Do nothing
                }
                is Resource.Loading -> {
                    _uiState.value = CategoryUiState.Loading
                }
            }
        }
    }

    fun loadCategoryDetail(categoryId: String) {
        viewModelScope.launch {
            when (val result = productRepository.getCategoryById(categoryId)) {
                is Resource.Success -> {
                    _categoryDetail.value = result.data
                }
                is Resource.Error -> {
                    _uiState.value = CategoryUiState.Error(result.error.message ?: "Không thể tải chi tiết danh mục")
                }
                is Resource.Idle -> {
                    // Do nothing
                }
                is Resource.Loading -> {
                    // Do nothing
                }
            }
        }
    }

    fun createCategory(name: String, logo: String?, parentCategoryId: String?) {
        viewModelScope.launch {
            when (val result = productRepository.createCategory(name, logo, parentCategoryId)) {
                is Resource.Success -> {
                    // Reload categories after creating
                    loadCategories(parentCategoryId)
                }
                is Resource.Error -> {
                    _uiState.value = CategoryUiState.Error(result.error.message ?: "Không thể tạo danh mục")
                }
                is Resource.Idle -> {
                    // Do nothing
                }
                is Resource.Loading -> {
                    // Do nothing
                }
            }
        }
    }

    fun updateCategory(categoryId: String, name: String, logo: String?, parentCategoryId: String?) {
        viewModelScope.launch {
            when (val result = productRepository.updateCategory(categoryId, name, logo, parentCategoryId)) {
                is Resource.Success -> {
                    loadCategories(parentCategoryId)
                }
                is Resource.Error -> {
                    _uiState.value = CategoryUiState.Error(result.error.message ?: "Không thể cập nhật danh mục")
                }
                is Resource.Idle -> {
                    // Do nothing
                }
                is Resource.Loading -> {
                    // Do nothing
                }
            }
        }
    }

    fun deleteCategory(categoryId: String, parentCategoryId: String? = null) {
        viewModelScope.launch {
            when (val result = productRepository.deleteCategory(categoryId)) {
                is Resource.Success -> {
                    loadCategories(parentCategoryId)
                }
                is Resource.Error -> {
                    _uiState.value = CategoryUiState.Error(result.error.message ?: "Không thể xóa danh mục")
                }
                is Resource.Idle -> {
                    // Do nothing
                }
                is Resource.Loading -> {
                    // Do nothing
                }
            }
        }
    }

    fun refresh() {
        loadCategories()
    }
}


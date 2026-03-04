package com.example.notebook

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.example.notebook.databinding.ActivityMainBinding
import com.example.notebook.ui.AiStudioFragment
import com.example.notebook.ui.AskFragment
import com.example.notebook.ui.HandwriteNoteFragment
import com.example.notebook.ui.HomeFragment
import com.example.notebook.ui.ImportFragment
import com.example.notebook.ui.KnowledgeCreateFragment
import com.example.notebook.ui.KnowledgeFilePreviewFragment
import com.example.notebook.ui.KnowledgeFragment
import com.example.notebook.ui.KnowledgeLibraryDetailFragment
import com.example.notebook.ui.NotesFragment
import com.example.notebook.ui.NoteDetailFragment
import com.example.notebook.ui.ProfileFragment
import com.example.notebook.ui.TemplateFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchRootPage(HomeFragment(), "NoteBook", "智能学习工作台")
                R.id.nav_notes -> switchRootPage(NotesFragment(), "我的笔记", "结构化记录与编排")
                R.id.nav_kb -> switchRootPage(KnowledgeFragment(), "知识库", "教材 / PPT / 讲义")
                R.id.nav_ai -> switchRootPage(AiStudioFragment(), "笔记生成", "添加与生成笔记")
                R.id.nav_profile -> switchRootPage(ProfileFragment(), "我的", "主题与偏好")
                else -> false
            }
        }

        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateTopBarForCurrentPage()
        }
    }

    fun openImportPage() {
        openDetailPage(ImportFragment(), "导入中心", "PDF 与拍照输入")
    }

    fun openAddNotePage() {
        binding.bottomNav.selectedItemId = R.id.nav_ai
    }

    fun openAskPage() {
        openDetailPage(AskFragment(), "笔记问答", "带引用的智能回答")
    }

    fun openTemplatePage() {
        openDetailPage(TemplateFragment(), "样式与排版", "风格预设与版式参数")
    }

    fun openNoteDetailPage(title: String, subtitle: String) {
        openDetailPage(NoteDetailFragment.newInstance(title, subtitle), title, "笔记详情")
    }

    fun openHandwriteNotePage() {
        openHandwriteNotePage("default", "手写笔记本")
    }

    fun openHandwriteNotePage(noteId: String, title: String) {
        openDetailPage(
            HandwriteNoteFragment.newInstance(noteId, title),
            title,
            "画笔 · 橡皮 · 套索 · 图形矫正 · 保存"
        )
    }

    fun openKnowledgeLibraryPage(libraryName: String, fileCount: String) {
        openDetailPage(
            KnowledgeLibraryDetailFragment.newInstance(libraryName, fileCount),
            libraryName,
            "知识库文件"
        )
    }

    fun openKnowledgeCreatePage() {
        openDetailPage(KnowledgeCreateFragment(), "新建知识库", "创建与管理课程知识库")
    }

    fun openKnowledgeFilePreviewPage(libraryName: String, fileName: String, fileType: String) {
        openDetailPage(
            KnowledgeFilePreviewFragment.newInstance(libraryName, fileName, fileType),
            fileName,
            "应用内查看"
        )
    }

    private fun switchRootPage(fragment: Fragment, title: String, subtitle: String): Boolean {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, fragment)
            .commit()
        setTopBar(title, subtitle)
        return true
    }

    private fun openDetailPage(fragment: Fragment, title: String, subtitle: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, fragment)
            .addToBackStack(title)
            .commit()
        setTopBar(title, subtitle)
    }

    private fun setTopBar(title: String, subtitle: String) {
        binding.toolbar.title = title
        binding.toolbar.subtitle = subtitle
    }

    private fun updateTopBarForCurrentPage() {
        when (supportFragmentManager.findFragmentById(R.id.content_container)) {
            is HomeFragment -> setTopBar("NoteBook", "智能学习工作台")
            is NotesFragment -> setTopBar("我的笔记", "结构化记录与编排")
            is NoteDetailFragment -> {
                val detail = supportFragmentManager.findFragmentById(R.id.content_container) as NoteDetailFragment
                setTopBar(detail.pageTitle(), "笔记详情")
            }
            is HandwriteNoteFragment -> {
                val detail = supportFragmentManager.findFragmentById(R.id.content_container) as HandwriteNoteFragment
                setTopBar(detail.pageTitle(), "画笔 · 橡皮 · 套索 · 图形矫正 · 保存")
            }
            is KnowledgeFragment -> setTopBar("知识库", "教材 / PPT / 讲义")
            is KnowledgeCreateFragment -> setTopBar("新建知识库", "创建与管理课程知识库")
            is KnowledgeLibraryDetailFragment -> {
                val detail = supportFragmentManager.findFragmentById(R.id.content_container) as KnowledgeLibraryDetailFragment
                setTopBar(detail.pageTitle(), "知识库文件")
            }
            is KnowledgeFilePreviewFragment -> {
                val detail = supportFragmentManager.findFragmentById(R.id.content_container) as KnowledgeFilePreviewFragment
                setTopBar(detail.pageTitle(), "应用内查看")
            }
            is AiStudioFragment -> setTopBar("笔记生成", "添加与生成笔记")
            is ProfileFragment -> setTopBar("我的", "主题与偏好")
            is ImportFragment -> setTopBar("导入中心", "PDF 与拍照输入")
            is AskFragment -> setTopBar("笔记问答", "带引用的智能回答")
            is TemplateFragment -> setTopBar("样式与排版", "风格预设与版式参数")
        }
    }
}

package com.hmncube.juiceme.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.hmncube.juiceme.R
import com.hmncube.juiceme.UserFeedback
import com.hmncube.juiceme.ViewModelFactory
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import com.hmncube.juiceme.databinding.FragmentHistoryBinding
import com.hmncube.juiceme.home.HomeFragment
import com.hmncube.juiceme.useCases.PreferencesUseCase

class HistoryFragment : Fragment(), OptionsMenuClickListener {
    private lateinit var viewModel: HistoryViewModel
    private lateinit var viewBinding: FragmentHistoryBinding
    private lateinit var adapter: HistoryAdapter

    private var codePrefix = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentHistoryBinding.inflate(inflater)
        return viewBinding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelFactory(AppDatabase.getDatabase(requireContext()))
            .create(HistoryViewModel::class.java)
        adapter = HistoryAdapter(this)
        setupMenu()

        codePrefix = PreferencesUseCase(requireContext()).getUssdCode() ?: ""

        viewBinding.historyRv.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.historyRv.adapter = adapter

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            toggleLoadingState(isLoading)
        }

        viewModel.history.observe(viewLifecycleOwner) { historyData ->
            if (historyData.isEmpty()) {
                toggleDataViews(View.VISIBLE, View.GONE)
            } else {
                adapter.setData(historyData.toMutableList())
                toggleDataViews(View.GONE, View.VISIBLE)
            }
        }
    }

    private fun toggleDataViews(emptyVisibility: Int, recyclerViewVisibility: Int) {
        viewBinding.emptyLayout.emtpyTv.visibility = emptyVisibility

        viewBinding.historyRv.visibility = recyclerViewVisibility
    }

    private fun toggleLoadingState(loading: Boolean) {
        if (loading) {
            viewBinding.historyProgressBar.visibility = View.VISIBLE
            viewBinding.historyRv.visibility = View.GONE
        } else {
            viewBinding.historyProgressBar.visibility = View.GONE
            viewBinding.historyRv.visibility = View.VISIBLE
        }
    }

    override fun onOptionsMenuClicked(cardNumber: CardNumber, position: Int) {
        val popupMenu = PopupMenu(requireContext() , viewBinding.historyRv[position].findViewById(R.id.dateTv))
        popupMenu.inflate(R.menu.options_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            handleMenuClicks(
                item = item,
                cardNumber = cardNumber,
                position = position
            )
        }
        popupMenu.show()
    }

    @SuppressWarnings("ReturnCount")
    private fun handleMenuClicks(item: MenuItem?, cardNumber: CardNumber, position: Int) : Boolean {
        when(item?.itemId){
            R.id.optionMenuDelete -> {
                showConfirmationDialog(cardNumber, position)
                return true
            }
            R.id.optionMenuRedial -> {
                HomeFragment.dialNumber(codePrefix, cardNumber.number, viewBinding.root, requireContext())
                return true
            }
            R.id.optionMenuCopy -> {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData =
                    ClipData.newPlainText(
                        requireContext().resources.getString(R.string.recharge_code), cardNumber.number
                    )
                clipboard.setPrimaryClip(clip)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    UserFeedback().displayFeedback(
                        requireView(), String.format(
                            resources.getString(R.string.copied_recharge_code), cardNumber.number),
                        UserFeedback.LENGTH_SHORT
                    )
                }
                return true
            }
        }
        return false
    }

    private fun showConfirmationDialog(cardNumber: CardNumber, position: Int) {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.menu_delete) { _, _ ->
                    viewModel.deleteEntry(cardNumber)
                    adapter.deletedItem(position)
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
            }
            builder.setMessage(R.string.delete_confirmation)?.setTitle(R.string.delete_confirmation_title)

            builder.create()
        }
        alertDialog?.show()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.history_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.historyOptionsClear) {
                    viewModel.clearAll()
                    adapter.clearAll()
                    toggleDataViews(
                        View.VISIBLE,
                        View.GONE
                    )
                    return true
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}

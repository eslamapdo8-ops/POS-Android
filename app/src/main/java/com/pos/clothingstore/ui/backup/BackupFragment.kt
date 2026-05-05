package com.pos.clothingstore.ui.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.Scopes
import com.pos.clothingstore.databinding.FragmentBackupBinding

/**
 * شاشة النسخ الاحتياطي إلى Google Sheets
 * MVP:placeholder - النسخ الاحتياطي اختياري
 */
class BackupFragment : Fragment() {

    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInClient: GoogleSignInClient
    private var signedInAccount: GoogleSignInAccount? = null

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            onSignInSuccess(account)
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "فشل تسجيل الدخول", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scopes.DRIVE_APPDATA)
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // التحقق من تسجيل الدخول السابق
        signedInAccount = GoogleSignIn.getLastSignedInAccount(requireActivity)
        updateSignedInUI()

        binding.btnGoogleSignin.setOnClickListener {
            signInLauncher.launch(googleSignInClient.signInIntent)
        }

        binding.btnBackup.setOnClickListener {
            Toast.makeText(requireContext(), "النسخ الاحتياطي سيتوفر في الإصدار القادم", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSignInSuccess(account: GoogleSignInAccount) {
        signedInAccount = account
        updateSignedInUI()
        Toast.makeText(requireContext(), "تم تسجيل الدخول: ${account.email}", Toast.LENGTH_SHORT).show()
    }

    private fun updateSignedInUI() {
        val account = signedInAccount
        if (account != null) {
            binding.tvSignedInAs.visibility = View.VISIBLE
            binding.tvSignedInAs.text = "مسجل الدخول: ${account.email}"
            binding.btnGoogleSignin.text = "تسجيل الخروج"
            binding.btnBackup.isEnabled = true
        } else {
            binding.tvSignedInAs.visibility = View.GONE
            binding.btnGoogleSignin.text = "تسجيل الدخول بحساب Google"
            binding.btnBackup.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
